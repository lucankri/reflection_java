package edu.school21.services;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public interface WorkingObject {
    Map<String, Object> fieldParser(Object o);
    <T> T createObjectAndSetFields(Class<T> clazz, Map<String, Object> values) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
