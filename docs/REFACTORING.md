# 🏦 Core Banking Back-Office System — Refactoring Journal

> A complete history of every architectural decision, refactor, and evolution of this project — from a simple monolithic CLI to a layered JavaFX desktop application with a planned PostgreSQL persistence layer.

---

## 📋 Table of Contents

1. [Project Evolution Overview](#1-project-evolution-overview)
2. [Phase 1 — Foundation](#2-phase-1--foundation-commits-ff1d708--c8708d6)
3. [Phase 2 — Domain Enrichment](#3-phase-2--domain-enrichment)
4. [Phase 3 — Service & Auth Layer](#4-phase-3--service--auth-layer)
5. [Phase 4 — BigDecimal & Precision Refactor](#5-phase-4--bigdecimal--precision-refactor)
6. [Phase 5 — View-Based Architecture (MVC Refactor)](#6-phase-5--view-based-architecture-mvc-refactor)
7. [Phase 6 — Repository Pattern & Clean Architecture](#7-phase-6--repository-pattern--clean-architecture)
8. [Phase 7 — JavaFX Presentation Layer](#8-phase-7--javafx-presentation-layer)
9. [Phase 8 — JDBC & PostgreSQL Integration Plan](#9-phase-8--jdbc--postgresql-integration-plan)
10. [Architecture Diagrams](#10-architecture-diagrams)
11. [Key Design Decisions](#11-key-design-decisions)

---

## 1. Project Evolution Overview

```
ff1d708  Initial commit
   │
   ▼
c8708d6  Banking System partially established
   │
   ▼  ── Phase 1: Domain Models (Customer, Account, Transaction)
   │
   ▼  ── Phase 2: BigDecimal precision, encapsulation
   │
   ▼  ── Phase 3: Service layer, Authentication, Authorization
   │
   ▼  ── Phase 4: Monolithic CLI → View-based MVC refactor
   │
   ▼  ── Phase 5: Repository Pattern, CustomerRepository
   │
   ▼  ── Phase 6: JavaFX Presentation Layer
   │
52c2f8b  HEAD — JavaFX app + JDBC plan ready
```

---

## 2. Phase 1 — Foundation

**Commits:** `ff1d708` → `c8708d6` → `420c6fc` → `4ee85af`

### What was built
The initial skeleton of the system — a basic `Account` class and a partially established banking system with no validation, no service layer, and no separation of concerns.

### Key commits
| Commit | Change |
|--------|--------|
| `ff1d708` | Initial commit |
| `c8708d6` | Banking System partially established |
| `420c6fc` | README documentation |
| `4ee85af` | Updated README |

### State of the codebase
```
BankingSystem.java  ← everything in one place
Account.java        ← basic fields, no validation
Customer.java       ← minimal
```

---

## 3. Phase 2 — Domain Enrichment

**Commits:** `9ba8fed` → `48ca965` → `5bc8d6d` → `9b3a900` → `73ed008` → `1529e71` → `dfac086`

### What changed

**`9ba8fed` — IdGenerator**
Introduced `IdGenerator` for UUID-based customer ID generation. First step toward unique identity management.

**`48ca965` — BankService**
Created `BankService` as the central orchestrator for customer management and account creation. This was the seed of the service layer.

**`5bc8d6d` — Customer account management**
Enhanced `Customer` class with `addAccount()` and proper account list management. Introduced the composition relationship between `Customer` and `Account`.

**`9b3a900` — National ID Validation**
First version of Egyptian National ID validation. Introduced `NationalIdValidator` with format and governorate code checks.

**`dfac086` — AccountType enum + validation**
Refactored `Account` class with proper validation and introduced `AccountType` enum (`SAVINGS` / `CURRENT`). First use of polymorphism hint.

**`1529e71` + `25956df` — Transaction recording**
Implemented `Transaction` recording inside account operations. Added `Instant` timestamp management.

### Design decision
> At this point the project was still tightly coupled — BankService, CLI, and domain logic were mixing. The next phases would separate them.

---

## 4. Phase 3 — Service & Auth Layer

**Commits:** `f5ff636` → `0cf1c29` → `f8af15c` → `337ff45` → `9486eb0` → `7358633` → `137a8aa`

### What changed

**`f5ff636` — Employee & Role classes**
Created `Employee` and `Role` (later renamed `EmployeeRole`) as first-class domain objects. Employees are now `Person` subtypes with role-based identity.

**`0cf1c29` — Authentication & Authorization**
Introduced `AuthenticationService` and `AuthorizationService` as separate concerns. Login validation and role checking extracted from the CLI.

**`f8af15c` — Login validation**
Hardened authentication with proper credential matching and exception throwing on failure.

**`337ff45` — ResourceNotFoundException & UnauthorizedException**
First custom exception hierarchy for the service layer. Operations now fail with meaningful typed exceptions instead of null returns or generic errors.

**`9486eb0` — AccountService**
Extracted `AccountService` from `BankService`. First step in decomposing the monolithic service.

**`7358633` — Employee & login refactor**
Improved `AuthenticationService` and `Employee` — better login validation, proper field management, password matching method.

**`137a8aa` — RBAC in deposit/withdrawal**
Role-Based Access Control enforced at the service layer for financial operations. Tellers can deposit/withdraw. CS cannot. Managers can do everything.

### Architecture at this point
```
BankEmployeeCLI  →  BankService
                 →  AuthenticationService
                 →  AuthorizationService
                 →  AccountService
```

---

## 5. Phase 4 — BigDecimal & Precision Refactor

**Commits:** `dcfd80d` → `19028c5` → `33d0495` → `f05a4c7` → `b073234`

### Why this mattered
Using `double` or `float` for financial amounts is a critical bug. `0.1 + 0.2 = 0.30000000000000004` in floating point. Banking systems require exact decimal arithmetic.

**`dcfd80d` — BigDecimal migration**
Replaced all monetary `double` values with `BigDecimal` across `Account`, `Transaction`, and related classes. This is a **breaking change** that touched every layer.

**`f05a4c7` — Map-based account storage**
Refactored account storage in `BankService` from `List<Account>` to `Map<String, Account>` for O(1) lookup by account number. First performance-aware data structure decision.

**`b073234` — BigDecimal normalization + CustomerRepository**
Critical fix: introduced `SCALE = 2` and `RoundingMode.HALF_UP` normalization so all balances have exactly 2 decimal places (`500.00` not `500`). Also introduced `CustomerRepository` as a dedicated data store — seeds the Repository Pattern.

```java
// Before
private double balance;

// After
private BigDecimal balance; // normalized to 2 decimal places
private static final int SCALE = 2;
private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
```

> This commit fixed a class of test failures where `assertEquals(500.00, balance)` was failing because `balance` was `500` without scale.

---

## 6. Phase 5 — View-Based Architecture (MVC Refactor)

**Commits:** `95e9569` → `6bff539` → `Add(feat) view-based architecture`

### The problem
`BankEmployeeCLI.java` had grown to 700+ lines. It was handling:
- Login UI
- Menu display
- Customer operations
- Account operations
- Transaction operations
- Input validation
- Display formatting

This violated **Single Responsibility Principle** and made the class unmaintainable.

### The refactor

**`95e9569` — LoginView + MenuView**
Extracted login handling and menu display into dedicated view classes. `BankEmployeeCLI` became a thin controller.

**`6bff539` — CustomerView, AccountView, TransactionView, InputValidator**
Full MVC decomposition:

```
Before:                          After:
BankEmployeeCLI.java (700+ lines)   BankEmployeeCLI.java (~100 lines)
                                    ├── LoginView.java
                                    ├── MenuView.java
                                    ├── CustomerView.java
                                    ├── AccountView.java
                                    ├── TransactionView.java
                                    └── InputValidator.java
```

### Benefits achieved
- Each view class: ~100–200 lines
- `InputValidator` reused across all views (DRY principle)
- `CustomerView.findCustomerByNationalId()` shared by `AccountView` and `TransactionView`
- Business logic untouched — only presentation changed

---

## 7. Phase 6 — Repository Pattern & Clean Architecture

**Commits:** `b073234` → `cd00e9b` → `c23d4d9` → `f13f3ed`

### What changed

**`cd00e9b` — Project structure reorganization**
Moved source files under `main/java` and added `test/` directory. Standard Maven layout enforced. This enabled proper JUnit 5 integration.

**`c23d4d9` — Role rename: Role → EmployeeRole**
Renamed `Role` enum to `EmployeeRole` across the entire codebase for clarity and consistency. Updated all references in `Employee`, `AuthorizationService`, `AuthenticationService`, and controllers.

**`f13f3ed` — Withdrawal fee tests**
Updated all JUnit tests to account for the 1% withdrawal fee. Tests now calculate `expectedBalance = deposit - (withdrawal + withdrawal * 0.01)` explicitly. This hardened the test suite significantly.

### Repository pattern finalized
```
BankService
  ├── CustomerRepository   ← findByNationalId(), save(), clear()
  ├── AccountRepository    ← findByNumber(), exists(), save()
  └── TransactionRepository ← findByAccountNumber(), save()
```

All repositories support `clear()` for test isolation via `BankService.reset()`.

---

## 8. Phase 7 — JavaFX Presentation Layer

**Commits:** `e13dd59` → `eab2f46` → `09fa2ec` → `9f02967` → `7533cca` → `e718c8a` → `ea85a86`

### Architecture decision
JavaFX was added as a **separate presentation layer** that calls only the Service Layer. It never touches repositories directly — maintaining Clean Architecture.

```
JavaFX Controllers  →  BankService  →  Repositories  →  In-Memory
```

**`e13dd59` — MainApp + Navigation**
`MainApp.java` added as the JavaFX entry point. `NavigationManager` introduced for screen switching. `SessionManager` handles the currently logged-in employee across screens.

**`eab2f46` — FXML files**
All FXML layouts created:
- `login.fxml`
- `dashboard.fxml`
- `customer_form.fxml` / `customer_list.fxml`
- `account_form.fxml` / `account_list.fxml`
- `transaction_form.fxml` / `transaction_history.fxml`

**`09fa2ec` — EmployeeAware interface**
Introduced `EmployeeAware` interface implemented by all controllers that need the current employee. `SessionManager` injects the employee after every screen navigation.

```java
public interface EmployeeAware {
    void setEmployee(Employee employee);
}
```

**`9f02967` — Dynamic fee calculation in UI**
`TransactionFormController` enhanced with real-time fee calculation display. When a teller types a withdrawal amount, the UI shows the 1% fee and total deduction live — before confirming.

**`7533cca` — Readability refactor**
`TransactionFormController` and `TransactionHistoryController` cleaned up. FXML column widths adjusted for proper table layout.

**`e718c8a` — Enhanced export**
Transaction export refactored: customer-specific export and all-transactions export. File handling improved with proper append-only logic and duplicate prevention.

**`ea85a86` — Initial FXML resources**
Final FXML files and resource structure committed. CSS theme (`bank-theme.css`) added for consistent banking UI.

### Final JavaFX structure
```
presentation/
├── controllers/
│   ├── LoginController.java
│   ├── DashboardController.java
│   ├── DashboardHomeController.java
│   ├── CustomerFormController.java
│   ├── CustomerListController.java
│   ├── AccountFormController.java
│   ├── AccountListController.java
│   ├── TransactionFormController.java
│   ├── TransactionHistoryController.java
│   └── EmployeeAware.java           ← interface for session injection
└── util/
    ├── NavigationManager.java
    ├── SessionManager.java
    └── AlertHelper.java
```

---

## 9. Phase 8 — JDBC & PostgreSQL Integration Plan

> **Status:** Planned — next milestone  
> **Document:** [`docs/PostgreSQL_JDBC_Plan.docx`](./PostgreSQL_JDBC_Plan.docx)

### Why PostgreSQL

The current system uses in-memory repositories — data is lost on every restart. The next step is replacing those repositories with a real persistence layer.

| | SQLite | MySQL | **PostgreSQL** |
|---|---|---|---|
| Server-based | ❌ | ✅ | ✅ |
| Full ACID | ⚠️ | ✅ | ✅ |
| Stored Procedures | ❌ | ⚠️ | ✅ |
| Banking standard | ❌ | ⚠️ | ✅ |
| Spring Boot default | ❌ | ⚠️ | ✅ |

### Database Schema

Four tables mapped directly to the domain model:

```sql
-- ENUMs
CREATE TYPE employee_role AS ENUM ('CS', 'TELLER', 'MANAGER');
CREATE TYPE account_type  AS ENUM ('SAVINGS', 'CURRENT');
CREATE TYPE txn_type      AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER');

-- Tables
employees     ── id, username, password, national_id, role, is_active
customers     ── id, name, national_id, email, phone, is_deleted
accounts      ── account_number, customer_id (FK), type, balance NUMERIC(19,4), overdraft_limit
transactions  ── id, account_number (FK), type, amount, fee, total, balance_after, employee_id (FK)

-- Indexes
CREATE INDEX idx_customers_national_id  ON customers(national_id);
CREATE INDEX idx_accounts_customer_id   ON accounts(customer_id);
CREATE INDEX idx_transactions_account   ON transactions(account_number);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp DESC);
```

> `NUMERIC(19,4)` is used for all monetary values — never `FLOAT` or `DOUBLE`.

### New Package: `infrastructure/`

The JDBC layer will be added as a new package without touching existing code:

```
repository/          ← kept as-is (used by JUnit tests)
infrastructure/
├── DatabaseConnection.java   ← HikariCP connection pool
└── dao/
    ├── CustomerDao.java      ← replaces CustomerRepository in production
    ├── AccountDao.java       ← replaces AccountRepository in production
    └── TransactionDao.java   ← replaces TransactionRepository in production
```

### ACID Transactions in JDBC

The most critical pattern — the withdraw operation uses manual transaction management:

```java
connection.setAutoCommit(false); // START TRANSACTION
try {
    // 1. SELECT ... FOR UPDATE  (lock the row — prevent race conditions)
    // 2. Validate balance / overdraft rules
    // 3. UPDATE accounts SET balance = new_balance
    // 4. INSERT INTO transactions (audit record)
    connection.commit();         // COMMIT ✅
} catch (Exception e) {
    connection.rollback();       // ROLLBACK ❌ — data stays consistent
    throw e;
} finally {
    connection.setAutoCommit(true);
}
```

### Stored Procedures

Three stored procedures planned in PL/pgSQL:

| Procedure | Purpose |
|-----------|---------|
| `sp_deposit(account, amount, employee...)` | Atomic deposit + audit insert |
| `sp_withdraw(account, amount, employee...)` | Atomic withdraw with overdraft check + `FOR UPDATE` lock |
| `sp_get_account_statement(account, from, to)` | Filtered transaction history |

### Implementation Roadmap

| Step | Task | Estimated Time |
|------|------|----------------|
| 1 | Install PostgreSQL, create database | 1 day |
| 2 | Run DDL script | 2 hours |
| 3 | Add Maven dependencies (PostgreSQL driver + HikariCP) | 30 min |
| 4 | `DatabaseConnection.java` with HikariCP | 2 hours |
| 5 | `CustomerDao.java` | 3 hours |
| 6 | `AccountDao.java` | 3 hours |
| 7 | `TransactionDao.java` with ACID transactions | 4 hours |
| 8 | Stored Procedures in PostgreSQL | 4 hours |
| 9 | Wire into `BankService` via config | 2 hours |
| 10 | Run full JUnit test suite | 3 hours |

### Spring Boot Migration Path (future)

Once JDBC is complete, migration to Spring Boot is straightforward:

```
JDBC (Now)                         Spring Boot (Future)
──────────────────────────────────────────────────────
CustomerDao (manual SQL)       →   CustomerRepository extends JpaRepository
PreparedStatement boilerplate  →   Zero SQL for basic CRUD
Manual commit/rollback         →   @Transactional annotation
ResultSet mapping              →   @Entity auto-mapping
```

---

## 10. Architecture Diagrams

### Current Architecture (JavaFX + In-Memory)

```
┌─────────────────────────────────────────────┐
│              Presentation Layer             │
│   JavaFX Controllers  │  CLI Views          │
│   (presentation/)     │  (view/)            │
└──────────────┬──────────────────────────────┘
               │ calls only
┌──────────────▼──────────────────────────────┐
│               Service Layer                 │
│  BankService  │  AuthenticationService      │
│  AccountService  │  TransactionService      │
│  AuthorizationService                       │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│             Repository Layer                │
│  CustomerRepository  │  AccountRepository   │
│  TransactionRepository                      │
│           (in-memory HashMap)               │
└─────────────────────────────────────────────┘
```

### Target Architecture (after JDBC integration)

```
┌─────────────────────────────────────────────┐
│              Presentation Layer             │
│   JavaFX Controllers  │  CLI Views          │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│               Service Layer                 │
│         (ZERO changes needed)               │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│        Infrastructure Layer (NEW)           │
│  CustomerDao  │  AccountDao                 │
│  TransactionDao  │  DatabaseConnection      │
│         (JDBC + HikariCP)                   │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│            PostgreSQL Database              │
│  employees │ customers │ accounts           │
│  transactions                               │
│  + Stored Procedures                        │
└─────────────────────────────────────────────┘
```

---

## 11. Key Design Decisions

### Decision 1: BigDecimal over double
Financial amounts must use `NUMERIC(19,4)` in SQL and `BigDecimal` in Java. Floating point arithmetic is unsuitable for money.

### Decision 2: Repository Pattern
In-memory repositories are kept for unit testing. JDBC DAOs will be used in production. `BankService.reset()` enables clean test isolation.

### Decision 3: Authorization at Service Layer
Role checks happen inside `AuthorizationService`, not in the UI. This means both the CLI and JavaFX UI are protected by the same rules — no duplication.

### Decision 4: Immutable Transactions
`Transaction` objects are immutable after creation. The transaction log is an append-only audit trail — records are never updated or deleted.

### Decision 5: Soft Delete
Customers and accounts use `is_deleted` / `is_active` flags instead of hard deletes. Banking systems require full audit history.

### Decision 6: Denormalized employee info in transactions
`employee_name` and `employee_role` are stored directly in the `transactions` table (not just the FK). This ensures the audit log remains accurate even if an employee's details change later.

### Decision 7: FOR UPDATE in withdraw
The withdraw stored procedure uses `SELECT ... FOR UPDATE` to lock the account row. This prevents race conditions if two tellers attempt to withdraw from the same account simultaneously.

---

*This document is auto-generated from the git commit history and reflects the actual evolution of the codebase.*