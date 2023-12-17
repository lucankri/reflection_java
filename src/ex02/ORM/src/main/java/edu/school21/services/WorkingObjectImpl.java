package edu.school21.services;

import edu.school21.annotations.OrmColumn;
import edu.school21.annotations.OrmColumnId;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class WorkingObjectImpl implements WorkingObject{

    @Override
    public Map<String, Object> fieldParser(Object o) {
        Map<String, Object> result = new HashMap<>();
        Class<?> clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (field.isAnnotationPresent(OrmColumnId.class)) {
                    result.put("id", field.get(o));
                }
                if (field.isAnnotationPresent(OrmColumn.class)) {
                    OrmColumn ormColumn = field.getAnnotation(OrmColumn.class);
                    result.put(ormColumn.name(), field.get(o));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
        return result;
    }

    @Override
    public <T> T createObjectAndSetFields(Class<T> clazz, Map<String, Object> values) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        T entity = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(OrmColumnId.class) || field.isAnnotationPresent(OrmColumn.class)) {
                OrmColumn ormColumnAnnotation = field.getAnnotation(OrmColumn.class);
                OrmColumnId ormColumnAnnotationId = field.getAnnotation(OrmColumnId.class);
                field.setAccessible(true);
                if (ormColumnAnnotation != null) {
                    field.set(entity, values.get(ormColumnAnnotation.name()));
                }
                if (ormColumnAnnotationId != null) {
                    field.set(entity, values.get("id"));
                }
            }
        }

        return entity;
    }
}
