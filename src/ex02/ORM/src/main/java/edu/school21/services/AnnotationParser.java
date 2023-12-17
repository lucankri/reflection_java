package edu.school21.services;

import edu.school21.annotations.OrmColumn;
import edu.school21.annotations.OrmColumnId;
import edu.school21.annotations.OrmEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.Function;

public class AnnotationParser implements Function<Class<?>, EntityDesc> {
    @Override
    public EntityDesc apply(Class<?> entityClass) {
        EntityDesc result = new EntityDesc();
        result.columns = new ArrayList<>();
        boolean flagOrmEntity = false;
        OrmEntity ormEntityAnnotation = entityClass.getAnnotation(OrmEntity.class);
        if (ormEntityAnnotation != null) {
            result.tableName = ormEntityAnnotation.table();
            flagOrmEntity = true;
        }

        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            EntityDesc.ColumnDesc columnDesc = new EntityDesc.ColumnDesc();
            Class<?> javaTypeField = field.getType();
            String sqlTypeField = "";
            String columnName = "";
            if (field.isAnnotationPresent(OrmColumnId.class)) {
                columnName = "id";
                sqlTypeField = javaToSqlType(javaTypeField, 200);
                result.id = columnDesc;
            }

            if (field.isAnnotationPresent(OrmColumn.class)) {
                OrmColumn ormColumn = field.getAnnotation(OrmColumn.class);
                columnName = ormColumn.name();
                int columnLengthName = ormColumn.length();
                sqlTypeField = javaToSqlType(javaTypeField, columnLengthName);
                result.columns.add(columnDesc);
            }
            columnDesc.name = columnName;
            columnDesc.javaType = javaTypeField;
            columnDesc.sqlType = sqlTypeField;

        }
        if (!flagOrmEntity) {
            result = null;
        }
        return result;
    }

    protected String javaToSqlType(Class<?> type, int columnLengthName) {
        if (type == String.class) {
            return "VARCHAR(" + columnLengthName + ")";
        } else if (type == Integer.class) {
            return "INT";
        } else if (type == Double.class) {
            return "DOUBLE PRECISION";
        } else if (type == Boolean.class) {
            return "BOOLEAN";
        } else if (type == Long.class) {
            return "BIGINT";
        }
        return "";
    }
}
