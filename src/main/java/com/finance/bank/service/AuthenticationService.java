package com.finance.bank.service;

import com.finance.bank.config.DatabaseConfig;
import com.finance.bank.exception.AuthenticationException;
import com.finance.bank.model.Employee;
import com.finance.bank.model.EmployeeRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthenticationService {

    public Employee login(String username, String password) {

        if (username == null || password == null) {
            throw new AuthenticationException("Username and password are required");
        }

        username = username.trim();

        String sql = """
                SELECT system_id, username, national_id, password, role, email, phone
                FROM employees
                WHERE username = ? AND password = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                            rs.getString("system_id"),
                            rs.getString("username"),
                            rs.getString("national_id"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("password"),
                            EmployeeRole.valueOf(rs.getString("role"))
                    );
                }
            }

        } catch (Exception e) {
            throw new AuthenticationException("Database error: " + e.getMessage());
        }

        throw new AuthenticationException("Invalid username or password");
    }
}