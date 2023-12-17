package edu.school21.services;

import edu.school21.repositories.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrmManagerImpl implements OrmManager {
    private final List<EntityDesc> entities = new ArrayList<>();
    private final DataSource dataSource;
    private final Supplier<List<Class<?>>> modelsSupplier;
    private final Function<Class<?>, EntityDesc> annotationParser;
    private final WorkingObject workingObject;
    public OrmManagerImpl(Function<Class<?>, EntityDesc> annotationParser,
                          Supplier<List<Class<?>>> modelsSupplier, WorkingObject workingObject, DataSource dataSource) {
        this.annotationParser = annotationParser;
        this.modelsSupplier = modelsSupplier;
        this.dataSource = dataSource;
        this.workingObject = workingObject;
    }

    @Override
    public void initialize() {
        List<Class<?>> classes = modelsSupplier.get();

        entities.clear();
        entities.addAll(classes.stream()
                .map(annotationParser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        if (!entities.isEmpty()) {
            processEntity();
        } else {
            throw new RuntimeException("No entities to process");
        }
    }

    @Override
    public void save(Object entity) {
        EntityDesc entityDesc = findTableObject(entity.getClass());
        if (entityDesc == null) {
            throw new IllegalArgumentException("There is no such entity");
        }
        StringBuilder sqlInsertQuery = new StringBuilder("INSERT INTO ").append(entityDesc.tableName).append(" (");
        if (entityDesc.id != null) {
            sqlInsertQuery.append(entityDesc.id.name).append(", ");
        }
        for (EntityDesc.ColumnDesc column : entityDesc.columns) {
            sqlInsertQuery.append(column.name).append(", ");
        }
        sqlInsertQuery.delete(sqlInsertQuery.length() - 2, sqlInsertQuery.length());
        sqlInsertQuery.append(") VALUES (");
        for (int i = 0; i < entityDesc.columns.size(); i++) {
            sqlInsertQuery.append("?, ");
        }
        if (entityDesc.id != null) {
            sqlInsertQuery.append("?)");
        } else {
            sqlInsertQuery.delete(sqlInsertQuery.length() - 2, sqlInsertQuery.length());
            sqlInsertQuery.append(")");
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlInsertQuery.toString())) {
            int parameterIndex = 1;
            Map<String, Object> entityField = workingObject.fieldParser(entity);
            if (entityDesc.id != null) {
                preparedStatement.setObject(parameterIndex, entityField.get("id"));
                parameterIndex++;
            }
            for (EntityDesc.ColumnDesc column : entityDesc.columns) {
                preparedStatement.setObject(parameterIndex, entityField.get(column.name));
                parameterIndex++;
            }
            System.out.println(sqlInsertQuery);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Object entity) throws SQLException {
        EntityDesc entityDesc = findTableObject(entity.getClass());
        if (entityDesc == null) {
            throw new IllegalArgumentException("There is no such entity");
        }
        if (entityDesc.id == null) {
            throw new SQLException("The object has no ID");
        }
        StringBuilder sqlUpdateQuery = new StringBuilder("UPDATE ").append(entityDesc.tableName).append(" SET ");
        for (EntityDesc.ColumnDesc field : entityDesc.columns) {
            sqlUpdateQuery.append(field.name).append(" = ?, ");
        }
        sqlUpdateQuery.delete(sqlUpdateQuery.length() - 2, sqlUpdateQuery.length());
        sqlUpdateQuery.append(" WHERE ").append(entityDesc.id.name).append(" = ?");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdateQuery.toString())) {
            int parameterIndex = 1;
            Map<String, Object> entityField = workingObject.fieldParser(entity);
            for (EntityDesc.ColumnDesc column : entityDesc.columns) {
                preparedStatement.setObject(parameterIndex, entityField.get(column.name));
                parameterIndex++;
            }
            preparedStatement.setObject(parameterIndex, entityField.get("id"));
            System.out.println(sqlUpdateQuery);
            if (preparedStatement.executeUpdate() <= 0) {
                throw new SQLException("No data on id = " + entityField.get("id"));
            }
        }
    }

    @Override
    public <T> T findById(Long id, Class<T> aClass) throws SQLException {
        EntityDesc entityDesc = findTableObject(aClass);
        if (entityDesc == null) {
            throw new IllegalArgumentException("There is no such entity");
        }
        if (entityDesc.id == null) {
            throw new SQLException("The object has no ID");
        }
        String sqlSelectQuery = "SELECT * FROM " + entityDesc.tableName + " WHERE " + entityDesc.id.name + " = ?";
        Map<String, Object> values = new HashMap<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectQuery.toString())) {
            preparedStatement.setLong(1, id);
            System.out.println(sqlSelectQuery);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    values.put(entityDesc.id.name, resultSet.getObject(entityDesc.id.name));
                    for (EntityDesc.ColumnDesc column : entityDesc.columns) {
                        values.put(column.name, resultSet.getObject(column.name));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (!values.isEmpty()) {
                return workingObject.createObjectAndSetFields(aClass, values);
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processEntity() {
        for (EntityDesc entity : entities) {
            String dropTableQuery = "DROP TABLE IF EXISTS " + entity.tableName;
            StringBuilder createTableQuery = new StringBuilder("CREATE TABLE " + entity.tableName + " (");
            if (entity.id != null) {
                createTableQuery.append(entity.id.name).append(" ").append(entity.id.sqlType).append(" PRIMARY KEY, ");
            }
            for (EntityDesc.ColumnDesc columnDesc : entity.columns) {
                createTableQuery.append(columnDesc.name).append(" ").append(columnDesc.sqlType);
                createTableQuery.append(", ");
            }
            createTableQuery.setLength(createTableQuery.length() - 2);
            createTableQuery.append(")");
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(dropTableQuery);
                System.out.println("Generation: " + createTableQuery);
                statement.executeUpdate(createTableQuery.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private EntityDesc findTableObject(Class<?> clazz) {
        EntityDesc entityDesc = null;
        EntityDesc entityFactDesc = annotationParser.apply(clazz);
        for (EntityDesc e : entities) {
            if (e.tableName.equals(entityFactDesc.tableName)) {
                entityDesc = e;
            }
        }
        return entityDesc;
    }
}
