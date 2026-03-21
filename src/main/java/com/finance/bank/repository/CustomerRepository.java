package com.finance.bank.repository;

import com.finance.bank.config.DatabaseConfig;
import com.finance.bank.exception.DuplicateNationalIdException;
import com.finance.bank.exception.InvalidNationalIdException;
import com.finance.bank.model.Customer;
import com.finance.bank.util.IdGenerator;
import com.finance.bank.util.NationalIdValidator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer repository migrated from in-memory collections to database-backed persistence.
 *
 * <p>Before this transition, customer records were stored in process memory and were reset
 * whenever the application restarted. The current implementation persists customer data in
 * PostgreSQL using JDBC through {@link DatabaseConfig#getConnection()} and SQL operations.
 *
 * <p>The repository keeps the same public behavior (save/find/exists/list) so service-layer
 * workflows remain consistent while enabling durable storage, uniqueness enforcement at the
 * database level, and better support for multi-session usage.
 */
public class CustomerRepository {

    public Customer save(String name, String nationalId)
            throws DuplicateNationalIdException, InvalidNationalIdException {

        NationalIdValidator.validateNationalId(nationalId);

        if (existsByNationalId(nationalId)) {
            throw new DuplicateNationalIdException(
                    "Customer with National ID " + nationalId + " already exists"
            );
        }

        String systemId = IdGenerator.generateCustomerId();

        String sql = "INSERT INTO customers (system_id, name, national_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, systemId);
            ps.setString(2, name);
            ps.setString(3, nationalId);
            ps.executeUpdate();

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateNationalIdException(
                        "Customer with National ID " + nationalId + " already exists"
                );
            }
            throw new RuntimeException("Failed to save customer: " + e.getMessage(), e);
        }

        return new Customer(systemId, name, nationalId);
    }

    public Customer findByNationalId(String nationalId) {
        if (nationalId == null) return null;

        String sql = "SELECT system_id, name, national_id, email, phone FROM customers WHERE national_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nationalId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer: " + e.getMessage(), e);
        }

        return null;
    }

    public Customer findBySystemId(String systemId) {
        String sql = "SELECT system_id, name, national_id, email, phone FROM customers WHERE system_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, systemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer: " + e.getMessage(), e);
        }

        return null;
    }

    public boolean existsByNationalId(String nationalId) {
        String sql = "SELECT 1 FROM customers WHERE national_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nationalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check customer: " + e.getMessage(), e);
        }
    }

    public List<Customer> findAll() {
        String sql = "SELECT system_id, name, national_id, email, phone FROM customers ORDER BY name";

        List<Customer> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) result.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to list customers: " + e.getMessage(), e);
        }

        return result;
    }

    public void clear() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM transactions");
            st.executeUpdate("DELETE FROM accounts");
            st.executeUpdate("DELETE FROM customers");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear data: " + e.getMessage(), e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("system_id"),
                rs.getString("name"),
                rs.getString("national_id"),
                rs.getString("email"),
                rs.getString("phone")
        );
    }
}