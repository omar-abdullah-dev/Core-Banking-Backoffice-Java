package com.finance.bank.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides database connections via HikariCP connection pooling.
 * Configuration is loaded from database.properties in the classpath.
 */

/**
 * WHY HikariaCP insted of DriverManager?
 * 1. Performance: HikariCP is a high-performance connection pool that significantly reduces the overhead of establishing database connections.
 *                 DriverManager creates a new connection every time, which is costly.
 * 2. Resource Management: HikariCP manages a pool of connections, allowing for reuse and efficient handling of concurrent requests.
 *                         DriverManager does not provide pooling, leading to potential resource exhaustion under load.
 * 3. Configuration: HikariCP offers extensive configuration options for tuning performance, such as connection timeout,
 *                      pool size, and leak detection. DriverManager has no such capabilities.
 * 4. Reliability: HikariCP includes features like connection validation and automatic recovery from connection failures,
 *                  improving the robustness of the application. DriverManager does not handle these scenarios,
 *                  which can lead to application instability.
 * 5. Scalability: HikariCP is designed to scale with the application's needs, making it suitable for production environments.
 *                  DriverManager is more suitable for simple applications or testing scenarios where connection pooling is not required.
 * */
public class DatabaseConfig {

    private static volatile HikariDataSource dataSource;

    // Private constructor to prevent instantiation
    private DatabaseConfig() {}

    /**
     * Returns the shared HikariDataSource, initializing it on first call (lazy init).
     */
    private static HikariDataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DatabaseConfig.class) {
                if (dataSource == null) {
                    dataSource = buildDataSource();
                }
            }
        }
        return dataSource;
    }

    private static HikariDataSource buildDataSource() {
        Properties props = loadProperties();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "2")));
        config.setPoolName(props.getProperty("db.pool.name", "FinanceBankPool"));

        try {
            return new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database connection pool: " + e.getMessage(), e);
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input == null) {
                throw new RuntimeException("database.properties not found in classpath");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database.properties: " + e.getMessage(), e);
        }
        return props;
    }

    /**
     * Returns a connection from the pool.
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Shuts down the connection pool. Call this on application exit.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}