package com.finance.bank.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig    {

     // Database connection parameters

    private static final HikariDataSource DATA_SOURCE;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/finance_bank");
        config.setUsername("postgres");
        config.setPassword("Moharam@2002");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("FinanceBankPool");
        DATA_SOURCE = new HikariDataSource(config);
    }

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static void shutdown() {
        if (DATA_SOURCE != null && !DATA_SOURCE.isClosed()) {
            DATA_SOURCE.close();
        }
    }
}