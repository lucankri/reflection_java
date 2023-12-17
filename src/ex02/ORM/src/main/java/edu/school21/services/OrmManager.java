package edu.school21.services;

import java.sql.SQLException;

public interface OrmManager {
    void initialize();
    void save(Object entity);
    void update(Object entity) throws SQLException;
    <T> T findById(Long id, Class<T> aClass) throws SQLException;
}
