package com.finance.bank.repository;

import com.finance.bank.config.DatabaseConfig;
import com.finance.bank.exception.DuplicateAccountException;
import com.finance.bank.exception.InvalidAccountException;
import com.finance.bank.exception.ResourceNotFoundException;
import com.finance.bank.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    private final CustomerRepository customerRepository;

    public AccountRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void save(Account account) throws DuplicateAccountException {
        // prepared statement for inserting a new account, with conditional handling for overdraft limit based on account type
//        what :: stands for?
//          In SQL, the "::" operator is used for type casting.
//          It allows you to explicitly convert a value from one data type to another.
//          In the context of the provided SQL statement,
//          "?::account_type_enum" means that the parameter being set will be cast to the "account_type_enum" data type
//          defined in the database. This is necessary when inserting values into a column that expects a specific enum type,
//          ensuring that the value is correctly interpreted as that enum rather than a generic string or other data type.
        String sql = """
                INSERT INTO accounts (account_number, account_type, balance, overdraft_limit, customer_id)
                VALUES (?, ?::account_type_enum, ?, ?, ?)
                """;

        BigDecimal overdraft = null;
        if (account instanceof CurrentAccount ca) {
            overdraft = ca.getOverdraftLimit();
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, account.getAccountNumber());
            ps.setString(2, account.getAccountType().name());
            ps.setBigDecimal(3, account.getBalance());
            ps.setBigDecimal(4, overdraft);
            ps.setString(5, account.getOwner().getSystemId());
            ps.executeUpdate();

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateAccountException(
                        "Account " + account.getAccountNumber() + " already exists"
                );
            }
            throw new RuntimeException("Failed to save account: " + e.getMessage(), e);
        }
    }

    public void updateBalance(String accountNumber, BigDecimal newBalance) {
        // prepared statement for updating the balance of an existing account, identified by account number
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, newBalance);
            ps.setString(2, accountNumber);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update balance: " + e.getMessage(), e);
        }
    }

    public Account findByNumber(String accountNumber) {
        // prepared statement for retrieving an account by its account number, with mapping of result set to Account object
        String sql = """
                SELECT account_number, account_type, balance, overdraft_limit, customer_id
                FROM accounts WHERE account_number = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException | InvalidAccountException e) {
            throw new RuntimeException("Failed to find account: " + e.getMessage(), e);
        }

        return null;
    }

    public Account findByNumberOrThrow(String accountNumber) throws ResourceNotFoundException {
        Account account = findByNumber(accountNumber);
        if (account == null) {
            throw new ResourceNotFoundException("Account " + accountNumber + " not found");
        }
        return account;
    }

    public boolean exists(String accountNumber) {
        // prepared statement for checking the existence of an account by its account number, returning a boolean result
        String sql = "SELECT 1 FROM accounts WHERE account_number = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check account: " + e.getMessage(), e);
        }
    }

    public List<Account> findAll() {
        // prepared statement for retrieving all accounts, ordered by account number, with mapping of result set to a list of Account objects
        String sql = """
                        SELECT account_number, account_type, balance, overdraft_limit, customer_id 
                        FROM accounts ORDER BY account_number;
                    """;

        List<Account> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Account a = mapRow(rs);
                if (a != null) result.add(a);
            }

        } catch (SQLException | InvalidAccountException e) {
            throw new RuntimeException("Failed to list accounts: " + e.getMessage(), e);
        }

        return result;
    }

    public List<Account> findByCustomerId(String customerId) {
        // prepared statement to get accounts by customer ID, ordered by account number, with mapping of result set to a list of Account objects
        String sql = """
                SELECT account_number, account_type, balance, overdraft_limit, customer_id
                FROM accounts 
                WHERE customer_id = ? ORDER BY account_number
                """;

        List<Account> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account a = mapRow(rs);
                    if (a != null) result.add(a);
                }
            }

        } catch (SQLException | InvalidAccountException e) {
            throw new RuntimeException("Failed to find accounts by customer: " + e.getMessage(), e);
        }

        return result;
    }

    public void clear() {
        // simple statement to delete all records from accounts and transactions tables, ensuring referential integrity by deleting transactions first
        // no need to prepared statement in this case
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM transactions");
            st.executeUpdate("DELETE FROM accounts");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear accounts: " + e.getMessage(), e);
        }
    }
    // mapping method to convert a ResultSet row into an Account object, handling both savings and current account types,
    // and associating the account with its owner (customer)
    private Account mapRow(ResultSet rs) throws SQLException, InvalidAccountException {
        String accountNumber = rs.getString("account_number");
        String typeStr       = rs.getString("account_type");
        BigDecimal balance   = rs.getBigDecimal("balance");
        BigDecimal overdraft = rs.getBigDecimal("overdraft_limit");
        String customerId    = rs.getString("customer_id");

        Customer owner = customerRepository.findBySystemId(customerId);
        if (owner == null) return null;

        AccountType type = AccountType.valueOf(typeStr);
        Account account;

        if (type == AccountType.SAVINGS) {
            account = new SavingsAccount(accountNumber, owner);
        } else {
            account = new CurrentAccount(accountNumber, owner,
                    overdraft != null ? overdraft : BigDecimal.ZERO);
        }

        // to support negative overdrafts
        account.loadPersistedBalance(balance);

        return account;
    }
}

