# 🏦 Finance Bank — Database Problem Set
### PostgreSQL + JDBC Practice Problems (Beginner → Advanced)

> All problems use the **Finance Bank schema** — the same tables from your project.
> Each part builds on the previous one.

---

## 📌 The Schema (Reference)

```sql
-- Run this before starting
CREATE TYPE employee_role AS ENUM ('CS', 'TELLER', 'MANAGER');
CREATE TYPE account_type  AS ENUM ('SAVINGS', 'CURRENT');
CREATE TYPE txn_type      AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER');

CREATE TABLE employees (
    id          VARCHAR(36)   PRIMARY KEY,
    username    VARCHAR(50)   UNIQUE NOT NULL,
    password    VARCHAR(255)  NOT NULL,
    national_id CHAR(14)      UNIQUE NOT NULL,
    role        employee_role NOT NULL,
    is_active   BOOLEAN       DEFAULT TRUE,
    created_at  TIMESTAMPTZ   DEFAULT NOW()
);

CREATE TABLE customers (
    id          VARCHAR(36)   PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    national_id CHAR(14)      UNIQUE NOT NULL,
    email       VARCHAR(100),
    phone       VARCHAR(20),
    is_deleted  BOOLEAN       DEFAULT FALSE,
    created_at  TIMESTAMPTZ   DEFAULT NOW()
);

CREATE TABLE accounts (
    account_number  VARCHAR(16)   PRIMARY KEY,
    customer_id     VARCHAR(36)   NOT NULL REFERENCES customers(id),
    type            account_type  NOT NULL,
    balance         NUMERIC(19,4) DEFAULT 0.0000,
    overdraft_limit NUMERIC(19,4) DEFAULT 0.0000,
    is_active       BOOLEAN       DEFAULT TRUE,
    created_at      TIMESTAMPTZ   DEFAULT NOW()
);

CREATE TABLE transactions (
    id             VARCHAR(36)   PRIMARY KEY,
    account_number VARCHAR(16)   NOT NULL REFERENCES accounts(account_number),
    type           txn_type      NOT NULL,
    amount         NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    fee            NUMERIC(19,4) DEFAULT 0.0000,
    total          NUMERIC(19,4) NOT NULL,
    balance_after  NUMERIC(19,4) NOT NULL,
    employee_id    VARCHAR(36)   NOT NULL REFERENCES employees(id),
    employee_name  VARCHAR(100)  NOT NULL,
    employee_role  employee_role NOT NULL,
    timestamp      TIMESTAMPTZ   DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_customers_national_id ON customers(national_id);
CREATE INDEX idx_accounts_customer_id  ON accounts(customer_id);
CREATE INDEX idx_transactions_account  ON transactions(account_number);
```

---

## 🌱 Part 1 — Basic SQL (DDL + DML)

> Goal: get comfortable with the schema before writing any Java.

---

### Problem 1.1 — Insert seed data

Insert the following records directly in `psql`:

**Employees:**

| username | password | national_id | role |
|----------|----------|-------------|------|
| ahmed | ahmed123! | 30111111700915 | MANAGER |
| omar | omar1234! | 30212121700915 | CS |
| mohamed | mod12345! | 30111111700916 | TELLER |

**Customers:**

| name | national_id |
|------|-------------|
| Karim Hassan | 29901011234567 |
| Sara Ahmed | 30005021234568 |

**Accounts:**

| account_number | customer | type | balance |
|----------------|----------|------|---------|
| 1001000100000001 | Karim Hassan | SAVINGS | 5000.00 |
| 1001000100000002 | Sara Ahmed | CURRENT | 2000.00 |
| 1001000100000003 | Sara Ahmed | SAVINGS | 500.00 |

> 💡 Tip: you need to insert customers before accounts because of the FK constraint.

---

### Problem 1.2 — Basic SELECT

Write a query for each:

1. Get all active customers (not deleted).
2. Get all accounts where `type = 'SAVINGS'`.
3. Get all accounts where `balance > 1000`.
4. Get all employees where `role = 'TELLER'`.
5. Get the account number and balance of Sara Ahmed.

---

### Problem 1.3 — UPDATE & DELETE

1. Update Sara Ahmed's phone to `'01012345678'`.
2. Deactivate account `1001000100000003` (set `is_active = FALSE`).
3. Soft-delete Karim Hassan (set `is_deleted = TRUE`).

> ⚠️ Banking systems never hard-delete. Always use soft delete.

---

### Problem 1.4 — Simple aggregation

1. Count total number of accounts per customer.
2. Get the total balance of all active savings accounts.
3. Find the account with the highest balance.

---

## 🔗 Part 2 — JOINs & Relationships

> Goal: query across tables — the most common real-world SQL task.

---

### Problem 2.1 — Customer + Accounts

Write a single query that returns:
```
customer_name | account_number | account_type | balance
```
For all active customers with active accounts.

---

### Problem 2.2 — Transactions with employee info

Write a query that returns the last 5 transactions with:
```
timestamp | account_number | type | amount | balance_after | performed_by | role
```
Ordered by newest first.

---

### Problem 2.3 — Account statement

Write a query that returns all transactions for account `1001000100000001`, ordered by timestamp descending.

---

### Problem 2.4 — Customer full profile

Write a single query that returns for a given `national_id`:
```
customer_name | national_id | account_number | account_type | balance | total_transactions
```

> 💡 Tip: use `COUNT()` with `GROUP BY`.

---

### Problem 2.5 — Employees who made transactions

Find all employees who performed at least one transaction. Show their `username`, `role`, and `transaction_count`.

---

## 🛠️ Part 3 — Basic JDBC in Java

> Goal: connect Java to PostgreSQL and run your first queries.

---

### Problem 3.1 — Setup & connection test

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.2</version>
</dependency>
```

Write a `DatabaseConnection.java` class with a `getConnection()` method. Then write a `main()` that:
1. Opens a connection.
2. Prints `"Connected to Finance Bank DB ✅"`.
3. Closes the connection.

---

### Problem 3.2 — Find customer by national ID

Write a method:
```java
public Customer findByNationalId(String nationalId)
```

- Use `PreparedStatement` (not `Statement`).
- Return a `Customer` object if found, `null` if not.
- Map the `ResultSet` columns to the `Customer` fields.

> ⚠️ Never use `Statement` for user input — SQL injection risk.

---

### Problem 3.3 — Insert a new customer

Write a method:
```java
public Customer save(String name, String nationalId)
```

- Generate an ID using `IdGenerator.generateCustomerId()`.
- Insert the row using `PreparedStatement`.
- Return the created `Customer` object.
- If the national ID already exists, catch the `SQLException` and throw `DuplicateNationalIdException`.

> 💡 PostgreSQL error code for duplicate key: `"23505"`

```java
if (e.getSQLState().equals("23505")) {
    throw new DuplicateNationalIdException(...);
}
```

---

### Problem 3.4 — List all customers

Write a method:
```java
public List<Customer> findAll()
```

- Query all non-deleted customers.
- Map each row to a `Customer` object.
- Return as `List<Customer>`.

---

### Problem 3.5 — Find account by number

Write a method:
```java
public Account findByNumber(String accountNumber)
```

- Query `accounts` table.
- Map `type` column to `AccountType` enum using:
```java
AccountType type = AccountType.valueOf(rs.getString("type"));
```
- Return the correct subtype: `SavingsAccount` or `CurrentAccount`.

---

## ⚡ Part 4 — ACID Transactions in JDBC

> Goal: understand the most important concept in banking systems — atomicity.

---

### Problem 4.1 — Why autoCommit is dangerous

Read this scenario and answer the questions:

```
1. Teller starts a withdrawal of 500 EGP
2. UPDATE accounts SET balance = balance - 505  ✅ succeeds
3. INSERT INTO transactions ...                 ❌ fails (network error)
4. Result: money deducted, no audit record
```

**Questions:**
1. What is wrong with this scenario?
2. How does `setAutoCommit(false)` + `commit()` + `rollback()` fix it?
3. What does "Atomic" mean in ACID?

---

### Problem 4.2 — Manual deposit with ACID

Write a `deposit()` method in `TransactionDao` that:

1. Opens a connection.
2. Sets `autoCommit = false`.
3. Updates the account balance.
4. Inserts a transaction record.
5. Calls `commit()`.
6. Calls `rollback()` in the `catch` block.
7. Resets `autoCommit = true` in `finally`.

```java
public Transaction deposit(Employee employee,
                           String accountNumber,
                           BigDecimal amount)
        throws InvalidAmountException {
    // your code here
}
```

---

### Problem 4.3 — Manual withdraw with overdraft check

Write a `withdraw()` method that:

1. Locks the account row with `SELECT ... FOR UPDATE`.
2. Reads `balance`, `overdraft_limit`, and `type`.
3. Calculates fee (1%) and total.
4. Checks the correct rule:
    - `SAVINGS`: balance after must be >= 0
    - `CURRENT`: balance after must be >= -overdraft_limit
5. Updates the balance.
6. Inserts the transaction record.
7. Commits or rolls back.

> 💡 `FOR UPDATE` prevents two tellers from withdrawing from the same account at the same time.

---

### Problem 4.4 — Test your ACID implementation

Write a JUnit test that verifies:

1. A deposit increases the balance correctly.
2. A withdrawal decreases the balance by `amount + 1% fee`.
3. A withdrawal that exceeds the balance throws `InsufficientAmountException` **and does not change the balance**.
4. After a failed withdrawal, the `transactions` table has **no new record**.

---

## 🔧 Part 5 — Stored Procedures

> Goal: move banking logic into the database for atomicity and performance.

---

### Problem 5.1 — Simple procedure: get account balance

Write a stored procedure:
```sql
CREATE OR REPLACE FUNCTION get_balance(p_account_number VARCHAR(16))
RETURNS NUMERIC AS $$
-- your code
$$ LANGUAGE plpgsql;
```

Call it from Java using `CallableStatement`.

---

### Problem 5.2 — sp_deposit procedure

Write a full `sp_deposit` stored procedure that:
1. Validates amount > 0 (raise exception if not).
2. Updates the account balance.
3. Inserts the transaction record.
4. Raises an exception if the account is not found or inactive.

Then call it from Java:
```java
String sql = "CALL sp_deposit(?, ?, ?, ?, ?::employee_role, ?)";
CallableStatement cs = conn.prepareCall(sql);
```

---

### Problem 5.3 — sp_withdraw procedure

Write `sp_withdraw` that:
1. Uses `FOR UPDATE` to lock the row.
2. Calculates fee = `amount * 0.01`.
3. Enforces SAVINGS / CURRENT overdraft rules.
4. Updates balance.
5. Inserts transaction.

---

### Problem 5.4 — sp_get_account_statement function

Write a function that returns a table of transactions for an account, with optional date filtering:

```sql
CREATE OR REPLACE FUNCTION sp_get_account_statement(
    p_account_number VARCHAR(16),
    p_from_date      TIMESTAMPTZ DEFAULT NULL,
    p_to_date        TIMESTAMPTZ DEFAULT NULL
)
RETURNS TABLE (...)
```

Call it from Java and map the results to `List<Transaction>`.

---

## 🚀 Part 6 — Wire into BankService

> Goal: replace the in-memory repositories with your new DAOs — zero changes to business logic.

---

### Problem 6.1 — Add HikariCP

Add to `pom.xml`:
```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
```

Update `DatabaseConnection.java` to use `HikariDataSource` instead of `DriverManager.getConnection()`.

**Questions:**
1. What is a connection pool?
2. Why is opening a new connection for every query slow?
3. What does `maximumPoolSize = 10` mean?

---

### Problem 6.2 — Swap repositories in BankService

`BankService` currently uses:
```java
private final CustomerRepository customerRepository;
private final AccountRepository accountRepository;
private final TransactionRepository transactionRepository;
```

Create a `ProductionBankServiceFactory` that builds a `BankService` with JDBC DAOs, and a `TestBankServiceFactory` that builds one with in-memory repositories.

> 💡 This keeps all existing JUnit tests passing without any changes.

---

### Problem 6.3 — Run the full test suite

Run `BankingSystemTest.java` with the JDBC DAOs wired in.

All 30 tests must pass. Fix any failures.

**Common issues to watch for:**
- `BankService.reset()` must truncate the database tables, not just clear in-memory maps.
- `NUMERIC(19,4)` results from PostgreSQL map to `BigDecimal` — scale may differ from in-memory. Use `compareTo()` not `equals()` for `BigDecimal` comparison in tests.

---

### Problem 6.4 — Final integration scenario

Run this full scenario end-to-end using your JDBC implementation:

```
1. Login as manager (from employees table)
2. Create customer: "Youssef Ali", national_id: "30001011234567"
3. Open SAVINGS account: 1001000100000099
4. Deposit 10,000 EGP (teller performs it)
5. Withdraw 3,000 EGP (teller performs it)
6. Query transaction history — verify 2 records
7. Verify final balance = 10000 - (3000 + 30 fee) = 6970.00
8. Restart the application — verify data persists ✅
```

Step 8 is the proof that PostgreSQL is working — data survives a restart, unlike in-memory storage.

---

## 📊 Summary

| Part | Topic | Key Concept |
|------|-------|-------------|
| 1 | Basic SQL | DDL, DML, soft delete |
| 2 | JOINs | Multi-table queries |
| 3 | Basic JDBC | PreparedStatement, ResultSet mapping |
| 4 | ACID Transactions | commit, rollback, FOR UPDATE |
| 5 | Stored Procedures | PL/pgSQL, CallableStatement |
| 6 | Integration | Swap repositories, full test suite |

---

*Schema and problems are based on the Finance Bank Core Banking Back-Office System.*