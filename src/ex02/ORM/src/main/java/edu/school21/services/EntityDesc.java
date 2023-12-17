package edu.school21.services;

import java.util.List;

public class EntityDesc {
    public static class ColumnDesc {
        public String name;
        public String sqlType; // INTEGER    VARCHAR(20)
        public Class<?> javaType; // Integer.class    String.class
    }

    public String tableName;
    public List<ColumnDesc> columns;
    public ColumnDesc id;
}
