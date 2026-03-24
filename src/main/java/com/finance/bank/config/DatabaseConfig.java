package com.finance.bank.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConfig is a utility class that provides a centralized way to manage database connections using HikariCP connection pooling.
 * It initializes a static HikariDataSource with the specified configuration parameters
 * and provides a method to obtain connections from the pool.
 *
*/

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
    // Private constructor to prevent instantiation
    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }
    public static void shutdown() {
        if (DATA_SOURCE != null && !DATA_SOURCE.isClosed()) {
            DATA_SOURCE.close();
        }
    }

    /**
     * We can also use DriverManager to connect to our database as the standard way to get our database connection.
     * However, using a connection pool like HikariCP provides several advantages:
     * 1. Performance: Connection pools maintain a pool of active connections that can be reused,
     *      reducing the overhead of establishing a new connection for each request.
     * 2. Resource Management: Connection pools manage the lifecycle of connections,
     *      ensuring that they are properly closed and returned to the pool after use,
     *      which helps prevent resource leaks.
     * 3. Scalability: Connection pools can handle a large number of concurrent requests
     *                  by efficiently managing the available connections,
     *                  which is crucial for applications with high traffic.
     * 4. Configuration: Connection pools offer various configuration options (e.g., maximum pool size, connection timeout)
     *          that allow fine-tuning of database connectivity based on application needs.
     * In contrast, using DriverManager directly can lead to performance issues and resource management challenges,
     *          especially in applications with high concurrency or long-running processes.
     * */

//     Here is how to setup the connection using DriverManager without connection pooling:
    public static Connection getConnectionWithDriverManager() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/finance_bank";
        String username = "postgres";
        String password = "Moharam@2002";
        return DriverManager.getConnection(url, username, password);
    }

}

