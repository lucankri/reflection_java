package edu.school21.repositories;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
    private final HikariDataSource data;

    public DataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://ep-snowy-voice-411646.us-east-2.aws.neon.tech/orm_bootcamp");
        config.setUsername("ArthurJann");
        config.setPassword("k37cptoDGjeb");

        data = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return data.getConnection();
    }
}

