# PostgreSQL + JDBC Integration Plan
## Finance Bank — Core Banking Back-Office System

---

## 1. Overview

This document outlines the complete plan to migrate the **Finance Bank Back-Office System** from its current **in-memory storage** (HashMap-based repositories) to a **PostgreSQL relational database** using **JDBC** (Java Database Connectivity).

The integration follows the existing **Clean Architecture** — only the **Repository Layer** will change. The Service Layer, Domain Models, and Presentation Layer remain untouched.

---

## 2. Why PostgreSQL + JDBC?

| Concern | In-Memory (Current) | PostgreSQL + JDBC |
|---|---|---|
| Data Persistence | ❌ Lost on restart | ✅ Permanent |
| Concurrency | ❌ Not thread-safe | ✅ ACID transactions |
| Scalability | ❌ Limited by RAM | ✅ Scales to millions of records |
| Reporting | ❌ Manual iteration | ✅ Full SQL power |
| Audit Logs | ❌ Volatile | ✅ Durable audit trail |
| Production-Ready | ❌ No | ✅ Yes |

---

## 3. Architecture After Migration

```
Presentation Layer (JavaFX)
         ↓
Service Layer (BankService, AuthenticationService, AuthorizationService)
         ↓
Repository Layer  ← ONLY THIS LAYER CHANGES
         ↓
JDBC Data Access Layer (NEW)
         ↓
PostgreSQL Database
```

**Zero changes** to:
- Domain models (`Customer`, `Account`, `Transaction`, `Employee`)
- Service layer business logic
- JavaFX controllers and views

---

## 4. Database Schema

### 4.1 employees Table

```sql
CREATE TABLE employees (
    system_id       VARCHAR(50)     PRIMARY KEY,
    username        VARCHAR(100)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    national_id     VARCHAR(14)     NOT NULL UNIQUE,
    email           VARCHAR(255),
    phone           VARCHAR(20),
    role            VARCHAR(20)     NOT NULL
                    CHECK (role IN ('CS', 'TELLER', 'MANAGER')),
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 customers Table

```sql
CREATE TABLE customers (
    system_id       VARCHAR(50)     PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    national_id     VARCHAR(14)     NOT NULL UNIQUE,
    email           VARCHAR(255),
    phone           VARCHAR(20),
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_national_id ON customers(national_id);
```

### 4.3 accounts Table

```sql
CREATE TABLE accounts (
    account_number  VARCHAR(16)     PRIMARY KEY,
    account_type    VARCHAR(10)     NOT NULL
                    CHECK (account_type IN ('SAVINGS', 'CURRENT')),
    balance         NUMERIC(15, 2)  NOT NULL DEFAULT 0.00,
    overdraft_limit NUMERIC(15, 2)  DEFAULT 0.00,
    owner_id        VARCHAR(50)     NOT NULL
                    REFERENCES customers(system_id) ON DELETE RESTRICT,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_accounts_owner_id ON accounts(owner_id);
```

### 4.4 transactions Table

```sql
CREATE TABLE transactions (
    transaction_id          VARCHAR(50)     PRIMARY KEY,
    account_number          VARCHAR(16)     NOT NULL
                            REFERENCES accounts(account_number) ON DELETE RESTRICT,
    transaction_type        VARCHAR(15)     NOT NULL
                            CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    amount                  NUMERIC(15, 2)  NOT NULL,
    fee                     NUMERIC(15, 2)  NOT NULL DEFAULT 0.00,
    total                   NUMERIC(15, 2)  NOT NULL,
    balance_after           NUMERIC(15, 2)  NOT NULL,
    performed_by_id         VARCHAR(50)     NOT NULL,
    performed_by_name       VARCHAR(255)    NOT NULL,
    performed_by_role       VARCHAR(20)     NOT NULL,
    timestamp               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_account  ON transactions(account_number);
CREATE INDEX idx_transactions_employee ON transactions(performed_by_id);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp DESC);
```

---

## 5. JDBC Configuration

### 5.1 Dependencies (pom.xml)

```xml
<!-- PostgreSQL JDBC Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>

<!-- HikariCP Connection Pool (recommended) -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
```

### 5.2 Database Configuration File

`src/main/resources/database.properties`

```properties
db.url=jdbc:postgresql://localhost:5432/financebank
db.username=bank_user
db.password=secure_password
db.pool.size=10
db.pool.timeout=30000
db.pool.idle-timeout=600000
db.pool.max-lifetime=1800000
```

### 5.3 DatabaseConfig Class (NEW)

```java
package com.finance.bank.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    private static HikariDataSource dataSource;

    static {
        try (InputStream input = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("database.properties")) {

            Properties props = new Properties();
            props.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(
                Integer.parseInt(props.getProperty("db.pool.size", "10"))
            );
            config.setConnectionTimeout(
                Long.parseLong(props.getProperty("db.pool.timeout", "30000"))
            );

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
```

---

## 6. New JDBC Repository Implementations

### 6.1 CustomerRepository (JDBC)

```java
package com.finance.bank.repository;

import com.finance.bank.exception.DuplicateNationalIdException;
import com.finance.bank.exception.InvalidNationalIdException;
import com.finance.bank.model.Customer;
import com.finance.bank.config.DatabaseConfig;
import com.finance.bank.util.IdGenerator;
import com.finance.bank.util.NationalIdValidator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {

    public Customer save(String name, String nationalId)
            throws DuplicateNationalIdException, InvalidNationalIdException {

        NationalIdValidator.validateNationalId(nationalId);

        String sql = """
            INSERT INTO customers (system_id, name, national_id)
            VALUES (?, ?, ?)
            """;

        String systemId = IdGenerator.generateCustomerId();

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, systemId);
            stmt.setString(2, name);
            stmt.setString(3, nationalId);
            stmt.executeUpdate();

            return new Customer(systemId, name, nationalId);

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // unique_violation
                throw new DuplicateNationalIdException(
                    "Customer with National ID " + nationalId + " already exists"
                );
            }
            throw new RuntimeException("Failed to save customer", e);
        }
    }

    public Customer findByNationalId(String nationalId) {
        String sql = "SELECT * FROM customers WHERE national_id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nationalId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRowToCustomer(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer", e);
        }
    }

    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers ORDER BY created_at DESC";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }
            return customers;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve customers", e);
        }
    }

    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getString("system_id"),
            rs.getString("name"),
            rs.getString("national_id"),
            rs.getString("email"),
            rs.getString("phone")
        );
    }
}
```

### 6.2 AccountRepository (JDBC)

```java
package com.finance.bank.repository;

import com.finance.bank.exception.DuplicateAccountException;
import com.finance.bank.model.*;
import com.finance.bank.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    public void save(Account account) throws DuplicateAccountException {
        String sql = """
            INSERT INTO accounts (account_number, account_type, balance, overdraft_limit, owner_id)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getAccountNumber());
            stmt.setString(2, account.getAccountType().name());
            stmt.setBigDecimal(3, account.getBalance());

            if (account instanceof CurrentAccount current) {
                stmt.setBigDecimal(4, current.getOverdraftLimit());
            } else {
                stmt.setBigDecimal(4, java.math.BigDecimal.ZERO);
            }

            stmt.setString(5, account.getOwner().getSystemId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                throw new DuplicateAccountException(
                    "Account " + account.getAccountNumber() + " already exists"
                );
            }
            throw new RuntimeException("Failed to save account", e);
        }
    }

    public void updateBalance(String accountNumber, java.math.BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, newBalance);
            stmt.setString(2, accountNumber);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account balance", e);
        }
    }

    public Account findByNumber(String accountNumber) {
        String sql = """
            SELECT a.*, c.name, c.national_id, c.email, c.phone
            FROM accounts a
            JOIN customers c ON a.owner_id = c.system_id
            WHERE a.account_number = ?
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRowToAccount(rs);
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to find account", e);
        }
    }

    public boolean exists(String accountNumber) {
        String sql = "SELECT 1 FROM accounts WHERE account_number = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check account existence", e);
        }
    }

    public List<Account> findAll() {
        String sql = """
            SELECT a.*, c.name, c.national_id, c.email, c.phone
            FROM accounts a
            JOIN customers c ON a.owner_id = c.system_id
            ORDER BY a.created_at DESC
            """;
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                accounts.add(mapRowToAccount(rs));
            }
            return accounts;

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve accounts", e);
        }
    }

    private Account mapRowToAccount(ResultSet rs) throws Exception {
        Customer owner = new Customer(
            rs.getString("owner_id"),
            rs.getString("name"),
            rs.getString("national_id"),
            rs.getString("email"),
            rs.getString("phone")
        );

        String type = rs.getString("account_type");

        if ("SAVINGS".equals(type)) {
            SavingsAccount acc = new SavingsAccount(rs.getString("account_number"), owner);
            acc.setBalance(rs.getBigDecimal("balance"));
            return acc;
        } else {
            CurrentAccount acc = new CurrentAccount(
                rs.getString("account_number"),
                owner,
                rs.getBigDecimal("overdraft_limit")
            );
            acc.setBalance(rs.getBigDecimal("balance"));
            return acc;
        }
    }

    public void clear() {
        // For test environment only
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM accounts");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear accounts", e);
        }
    }
}
```

### 6.3 TransactionRepository (JDBC)

```java
package com.finance.bank.repository;

import com.finance.bank.model.*;
import com.finance.bank.config.DatabaseConfig;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    public void save(Transaction transaction) {
        String sql = """
            INSERT INTO transactions (
                transaction_id, account_number, transaction_type,
                amount, fee, total, balance_after,
                performed_by_id, performed_by_name, performed_by_role,
                timestamp
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transaction.getTransactionId());
            stmt.setString(2, transaction.getAccountNumber());
            stmt.setString(3, transaction.getType().name());
            stmt.setBigDecimal(4, transaction.getAmount());
            stmt.setBigDecimal(5, transaction.getFee());
            stmt.setBigDecimal(6, transaction.getTotal());
            stmt.setBigDecimal(7, transaction.getBalanceAfter());
            stmt.setString(8, transaction.getPerformedByEmployeeId());
            stmt.setString(9, transaction.getPerformedByEmployeeName());
            stmt.setString(10, transaction.getPerformedByRole().name());
            stmt.setTimestamp(11, Timestamp.from(transaction.getTimestamp()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction", e);
        }
    }

    public List<Transaction> findByAccountNumber(String accountNumber) {
        String sql = """
            SELECT * FROM transactions
            WHERE account_number = ?
            ORDER BY timestamp ASC
            """;

        List<Transaction> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(mapRowToTransaction(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve transactions", e);
        }
    }

    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        List<Transaction> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(mapRowToTransaction(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve all transactions", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM transactions";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count transactions", e);
        }
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        // Reconstruct a lightweight Transaction for read-only display
        return new TransactionRecord(
            rs.getString("transaction_id"),
            TransactionType.valueOf(rs.getString("transaction_type")),
            rs.getBigDecimal("amount"),
            rs.getBigDecimal("fee"),
            rs.getBigDecimal("balance_after"),
            rs.getString("account_number"),
            rs.getString("performed_by_id"),
            rs.getString("performed_by_name"),
            EmployeeRole.valueOf(rs.getString("performed_by_role")),
            rs.getTimestamp("timestamp").toInstant()
        );
    }

    public void clear() {
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM transactions");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear transactions", e);
        }
    }
}
```

---

## 7. ACID Transaction Support

Critical operations (deposit/withdraw) must be wrapped in database transactions to guarantee consistency.

```java
// Example: Atomic deposit — balance update + transaction record in one DB transaction
public Transaction deposit(Employee employee, String accountNumber, BigDecimal amount)
        throws InvalidAmountException {

    authorizationService.ensureCanDeposit(employee);

    try (Connection conn = DatabaseConfig.getDataSource().getConnection()) {
        conn.setAutoCommit(false); // Begin JDBC transaction

        try {
            // 1. Lock and fetch account row
            Account account = accountRepository.findByNumberForUpdate(accountNumber, conn);
            account.deposit(amount);

            // 2. Update balance in DB
            accountRepository.updateBalance(accountNumber, account.getBalance(), conn);

            // 3. Record transaction
            Transaction tx = new Transaction(
                TransactionType.DEPOSIT, amount, BigDecimal.ZERO,
                account.getBalance(), account, employee
            );
            transactionRepository.save(tx, conn);

            conn.commit(); // Commit both writes atomically
            return tx;

        } catch (Exception e) {
            conn.rollback(); // Rollback on any failure
            throw e;
        }

    } catch (SQLException e) {
        throw new RuntimeException("Database error during deposit", e);
    }
}
```

---

## 8. Migration Steps

### Step 1 — Set Up PostgreSQL

```sql
-- Run as postgres superuser
CREATE DATABASE financebank;
CREATE USER bank_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE financebank TO bank_user;
```

### Step 2 — Run Schema Migration

```bash
psql -U bank_user -d financebank -f schema.sql
```

### Step 3 — Add Dependencies

Add `postgresql` and `HikariCP` to `pom.xml` (see Section 5.1).

### Step 4 — Add Configuration File

Create `src/main/resources/database.properties` (see Section 5.2).

### Step 5 — Create DatabaseConfig

Add `DatabaseConfig.java` to `com.finance.bank.config` package (see Section 5.3).

### Step 6 — Replace Repository Implementations

Replace all three repository classes with JDBC versions. The interfaces remain identical — BankService does not change.

### Step 7 — Update BankService Constructor

```java
private BankService() {
    // Before: in-memory repositories
    // this.customerRepository = new CustomerRepository();

    // After: JDBC repositories (same interface, different implementation)
    this.customerRepository = new CustomerRepository(); // now uses JDBC internally
    this.accountRepository  = new AccountRepository();
    this.transactionRepository = new TransactionRepository();

    // Services unchanged
    this.authorizationService = new AuthorizationService();
    this.accountService = new AccountService(authorizationService, accountRepository);
    this.transactionService = new TransactionService(
        authorizationService, accountService, transactionRepository
    );
}
```

### Step 8 — Seed Employee Data

```sql
INSERT INTO employees (system_id, username, password_hash, national_id, role)
VALUES
    (gen_random_uuid()::text, 'manager',  'manager123',  '29505051234567', 'MANAGER'),
    (gen_random_uuid()::text, 'teller',   'teller123',   '29505051234568', 'TELLER'),
    (gen_random_uuid()::text, 'cs',       'cs123456',    '29505051234569', 'CS');
```

> ⚠️ **Important:** Replace plain-text passwords with BCrypt hashes before any production deployment.

### Step 9 — Validate with Existing Test Suite

All existing JUnit tests in `BankingSystemTest.java` should pass without modification. The `BankService.reset()` method now issues `DELETE` statements instead of clearing HashMaps.

---

## 9. Project Structure After Migration

```
com.finance.bank/
├── app/
│   └── BankEmployeeCLI.java
├── config/                          ← NEW
│   └── DatabaseConfig.java
├── exception/                       (unchanged)
├── model/                           (unchanged)
├── presentation/                    ← JavaFX layer
│   ├── MainApp.java
│   └── controllers/
├── repository/                      ← JDBC implementations
│   ├── CustomerRepository.java
│   ├── AccountRepository.java
│   └── TransactionRepository.java
├── service/                         (unchanged)
│   ├── BankService.java
│   ├── AuthenticationService.java
│   ├── AuthorizationService.java
│   ├── AccountService.java
│   └── TransactionService.java
├── util/                            (unchanged)
└── view/                            (unchanged)

src/main/resources/
├── database.properties              ← NEW
└── schema.sql                       ← NEW
```

---

## 10. Future Enhancements

| Enhancement | Description |
|---|---|
| **Password Hashing** | Replace plain-text passwords with BCrypt via `spring-security-crypto` |
| **Flyway Migrations** | Version-controlled schema evolution with `org.flywaydb:flyway-core` |
| **Spring Data JPA** | Replace raw JDBC with JPA repositories for reduced boilerplate |
| **Spring Boot** | Add REST API layer for web/mobile client support |
| **Docker Compose** | Containerize PostgreSQL for reproducible dev/test environments |
| **Read Replicas** | Separate read/write connections for reporting queries |
| **Audit Table** | Dedicated `audit_log` table capturing every state change |
| **Soft Deletes** | Add `deleted_at` column instead of hard deletes for compliance |

---

## 11. Summary

| Phase | What Changes | What Stays the Same |
|---|---|---|
| Config | Add `DatabaseConfig`, `database.properties` | — |
| Schema | New PostgreSQL tables | — |
| Repositories | JDBC implementations | Public interfaces (same method signatures) |
| Services | Only constructor wiring | All business logic |
| Models | — | All domain objects |
| Tests | `reset()` uses `DELETE` | All assertions, all test scenarios |
| JavaFX UI | — | All controllers and views |

The key principle is **interface stability**: because `BankService` depends on repository method signatures — not implementations — swapping HashMap storage for PostgreSQL storage requires **zero changes** to any layer above the repository.

---

*Finance Bank Engineering Team — PostgreSQL JDBC Integration Plan v1.0*