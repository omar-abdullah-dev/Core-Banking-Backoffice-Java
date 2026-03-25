package com.finance.bank.repository;

import com.finance.bank.config.DatabaseConfig;
import com.finance.bank.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing transactions in the database.
 * Provides methods to save transactions and query them by account number or get all transactions.
 */
public class TransactionRepository {

    public void save(Transaction tx) {
        // prepared statement for inserting a new transaction, with explicit casting for enum types and handling of all transaction fields
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
        // prepared statement for selecting transactions by account number, ordered by timestamp, and mapping result set to Transaction objects
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
        // prepared statement for selecting all transactions, ordered by timestamp, and mapping result set to Transaction objects
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
        // prepared statement for counting total number of transactions in the database
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
        // prepared statement for counting number of transactions for a specific account number
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
        // statement to delete all transactions from the database, without affecting accounts or customers
        // prepared statement not needed in this case
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM transactions");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear transactions: " + e.getMessage(), e);
        }
    }
//    mapping method to convert a ResultSet row into a Transaction object, handling all fields and enum conversions
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