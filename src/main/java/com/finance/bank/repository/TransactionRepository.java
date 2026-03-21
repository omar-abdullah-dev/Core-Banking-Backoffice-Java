package com.finance.bank.repository;

import com.finance.bank.config.DatabaseConfig;
import com.finance.bank.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    public void save(Transaction tx) {
        String sql = """
                INSERT INTO transactions (
                    transaction_id, account_number, transaction_type,
                    amount, fee, total, balance_after, timestamp,
                    performed_by_employee_id, performed_by_name, performed_by_role
                ) VALUES (?, ?, ?::transaction_type_enum, ?, ?, ?, ?, ?, ?, ?, ?::employee_role_enum)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tx.getTransactionId());
            ps.setString(2, tx.getAccountNumber());
            ps.setString(3, tx.getType().name());
            ps.setBigDecimal(4, tx.getAmount());
            ps.setBigDecimal(5, tx.getFee());
            ps.setBigDecimal(6, tx.getTotal());
            ps.setBigDecimal(7, tx.getBalanceAfter());
            ps.setTimestamp(8, Timestamp.from(tx.getTimestamp()));
            ps.setString(9, tx.getPerformedByEmployeeId());
            ps.setString(10, tx.getPerformedByEmployeeName());
            ps.setString(11, tx.getPerformedByRole().name());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction: " + e.getMessage(), e);
        }
    }

    public List<Transaction> findByAccountNumber(String accountNumber) {
        String sql = """
                SELECT transaction_id, account_number, transaction_type,
                       amount, fee, total, balance_after, timestamp,
                       performed_by_employee_id, performed_by_name, performed_by_role
                FROM transactions
                WHERE account_number = ?
                ORDER BY timestamp ASC
                """;

        List<Transaction> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transactions: " + e.getMessage(), e);
        }

        return result;
    }

    public List<Transaction> findAll() {
        String sql = """
                SELECT transaction_id, account_number, transaction_type,
                       amount, fee, total, balance_after, timestamp,
                       performed_by_employee_id, performed_by_name, performed_by_role
                FROM transactions ORDER BY timestamp ASC
                """;

        List<Transaction> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) result.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transactions: " + e.getMessage(), e);
        }

        return result;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM transactions";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count transactions: " + e.getMessage(), e);
        }

        return 0;
    }

    public int countByAccount(String accountNumber) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE account_number = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count transactions: " + e.getMessage(), e);
        }

        return 0;
    }

    public void clear() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM transactions");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear transactions: " + e.getMessage(), e);
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getString("transaction_id"),
                TransactionType.valueOf(rs.getString("transaction_type")),
                rs.getBigDecimal("amount"),
                rs.getBigDecimal("fee"),
                rs.getBigDecimal("balance_after"),
                rs.getString("account_number"),
                rs.getTimestamp("timestamp").toInstant(),
                rs.getString("performed_by_employee_id"),
                rs.getString("performed_by_name"),
                EmployeeRole.valueOf(rs.getString("performed_by_role"))
        );
    }
}