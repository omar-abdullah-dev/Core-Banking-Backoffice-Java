# 🏦 Core Banking Back-Office System (Java, JavaFX)

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue?style=for-the-badge&logo=openjfx)
![JUnit5](https://img.shields.io/badge/JUnit-5.10.1-green?style=for-the-badge&logo=junit5)
![JDBC](https://img.shields.io/badge/JDBC-API-red?style=for-the-badge&logo=java)
![Maven](https://img.shields.io/badge/Maven-3.x-red?style=for-the-badge&logo=apachemaven)
![Version](https://img.shields.io/badge/Version-2.0.0-purple?style=for-the-badge)

**A production-grade core banking back-office application built with Java, featuring both CLI and JavaFX desktop interfaces.**

[Features](#-features) •
[Architecture](#-architecture) •
[Installation](#-installation) •
[Usage](#-how-to-run) •
[Testing](#-automated-testing)

</div>

---

## 📋 Overview

A comprehensive **Banking Employee Back-Office System** implemented in Java, designed using clean **Object-Oriented Programming (OOP)** principles and a **layered architecture**. The system features strong validation, custom exception handling, audit logging, automated testing, CSV export, and a full JavaFX Desktop GUI. **v2.0.0** completes the migration from in-memory repositories to PostgreSQL via JDBC/HikariCP while keeping the service and presentation layers stable (see `docs/PostgreSQL_JDBC_Plan.md`).

### Project Snapshot
- Title: Core Banking Back-Office System — Java 17, JavaFX 21, PostgreSQL 15+, JDBC/HikariCP, Maven, JUnit 5
- Highlights: RBAC-secured services, dual CLI/JavaFX interfaces, immutable audit-grade transactions with CSV export, and ACID-compliant persistence replacing in-memory stores
- Outcomes: 57 automated tests passing (unit + integration), documented JDBC migration plan, ready for fintech/back-office ATS keyword matching

This project simulates how bank employees manage customers, accounts, and transactions through:

| Interface | Description |
|-----------|-------------|
| 🖥️ **CLI Application** | Console-based interface for terminal users |
| 🪟 **JavaFX Desktop GUI** | Modern desktop application with rich UI |
| 🧪 **JUnit 5 Test Suite** | Comprehensive automated testing |
| 📤 **CSV Export System** | Transaction history export with multiple modes |
| 🔐 **RBAC Security** | Role-Based Access Control |

> ⚠️ **Scope Notice:** This system represents **employee-assisted banking operations only**.
> No customer self-service, online banking, or external transfers.

### Project Description

**Core Banking Back-Office System** is an enterprise-grade banking application for employee-assisted operations, built with **Java 17**, **JavaFX 21**, **PostgreSQL 15+**, **JDBC**, **HikariCP**, **Maven**, and **JUnit 5**. It implements:

- **Customer Lifecycle Management** — Account creation with Egyptian National ID validation (14-digit format, birth date extraction, governorate verification)
- **Account Management** — Dual account types (Savings/Current) with polymorphic withdrawal rules and configurable overdraft limits
- **Transaction Processing** — Deposits, withdrawals with 1% fee calculation, real-time balance validation, immutable audit trails
- **Role-Based Access Control (RBAC)** — Three roles (Customer Service, Teller, Manager) with permission enforcement at service layer
- **Persistent Storage** — PostgreSQL relational database with JDBC connectivity, HikariCP connection pooling (10 max, 2 min idle connections), 30-second timeouts
- **Dual Interfaces** — CLI (console) and JavaFX Desktop GUI with modern CSS theming, responsive sidebar navigation, real-time fee display
- **CSV Export System** — Three modes (by customer, all transactions, by account) with incremental append-only exports to `exports/` directory
- **Comprehensive Testing** — JUnit 5 suite with 57 passing automated tests covering authentication, authorization, customer creation, account operations, transaction processing, and exception scenarios
- **Clean Architecture** — Layered design (Presentation → Service → Repository → Domain) with design patterns (Singleton, Facade, Repository, Strategy, MVC, Observer)
- **Security Features** — Session management, masked account display, input validation, immutable transactions, complete audit trails
- **Database Transformation (v2.0.0)** — Migrated from in-memory HashMap/ArrayList to PostgreSQL with JDBC, maintaining full backward compatibility with service layer

The system demonstrates production-quality OOP principles, exception-driven design, enterprise architecture patterns, and ACID-compliant database integration—ideal for fintech, banking systems, and e-commerce transaction platforms.

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|----------|------------|---------|
| **Language** | Java | 17 (LTS) |
| **GUI Framework** | JavaFX | 21.0.2 |
| **Build Tool** | Maven | 3.x |
| **Testing** | JUnit Jupiter | 5.10.1 |
| **Architecture** | Layered / MVC | - |
| **Data Storage** | PostgreSQL with JDBC | 15+ |
| **Connection Pool** | HikariCP | 5.1.0 |
| **Logging** | SLF4J + Simple Logger | 1.7.36 |

---

## ✨ Features

### 🔐 Authentication & Authorization

| Role | Code | Permissions |
|------|------|-------------|
| **Customer Service** | `CS` | Create customers, Open accounts |
| **Teller** | `TELLER` | Deposit, Withdraw, View transactions |
| **Manager** | `MANAGER` | Full system access (all operations) |

- Employee login with username/password validation
- Role-based access control enforced at service layer
- Secure session management (login/logout)
- Authorization checks on every operation

### 👤 Customer Management

- Create customers with **Egyptian National ID validation**
- Comprehensive National ID validation:
  - 14-digit format validation
  - Birth date extraction and validation
  - Governorate code verification (35 valid codes)
  - Century detection (2xxx = 1900s, 3xxx = 2000s)
- Prevent duplicate customers (unique National ID)
- List customers with account count
- Search customers by National ID

### 💳 Account Management

| Account Type | Overdraft | Withdrawal Limit |
|--------------|-----------|------------------|
| **Savings Account** | ❌ No | Limited to balance |
| **Current Account** | ✅ Yes | Configurable limit |

- **16-digit account numbers** (system-generated)
- Multiple accounts per customer supported
- Account numbers masked in display (e.g., `XXXXXXXXXXXX3456`)
- Account validation with custom exceptions

### 💰 Transaction System

| Feature | Description |
|---------|-------------|
| **Deposit** | Add funds to any account (no fee) |
| **Withdrawal** | Remove funds with **1% fee** |
| **Fee Calculation** | Real-time fee display in GUI |
| **Balance Validation** | Polymorphic rules per account type |

**Transaction Record (Immutable):**
```
├── Transaction ID (unique)
├── Type (DEPOSIT / WITHDRAWAL)
├── Amount
├── Fee (1% for withdrawals)
├── Total (Amount + Fee)
├── Balance After
├── Timestamp
├── Account Number
└── Audit Trail
    ├── Employee ID
    ├── Employee Name
    └── Employee Role
```

### 📤 CSV Export System

**Three Export Modes:**

| Mode | Trigger | File Pattern |
|------|---------|--------------|
| **By Customer** | Filter by National ID | `transactions_customer_{nationalId}.csv` |
| **All Transactions** | Show All view | `transactions_all_{timestamp}_by_{employee}.csv` |
| **By Account** | Legacy/API | `transactions_account_{accountNumber}.csv` |

**Export Features:**
- Incremental export (append-only, no duplicates)
- Excel-compatible CSV format
- Automatic `exports/` directory creation
- Timestamp tracking per export key

**CSV Format:**
```csv
AccountNumber,Type,Amount,BalanceAfter,PerformedBy,Role,Timestamp,TransactionId
```

### 🇪🇬 Egyptian National ID Validation

The system validates Egyptian National IDs according to official format:

```
Position:  1  | 2-3 | 4-5 | 6-7 | 8-9  | 10-13 | 14
Content:   C  | YY  | MM  | DD  | GOV  | SEQ   | CHECK
```

| Component | Description |
|-----------|-------------|
| **Century (C)** | `2` = 1900s, `3` = 2000s |
| **Year (YY)** | Birth year (00-99) |
| **Month (MM)** | Birth month (01-12) |
| **Day (DD)** | Birth day (01-31) |
| **Governorate (GOV)** | 35 valid codes (01-35) |
| **Sequence (SEQ)** | Unique identifier |
| **Check (CHECK)** | Validation digit |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
├─────────────────────────────┬───────────────────────────────┤
│      CLI (Console)          │       JavaFX (Desktop)        │
│  ┌─────────────────────┐    │    ┌─────────────────────┐    │
│  │ BankEmployeeCLI     │    │    │ MainApp             │    │
│  │ LoginView           │    │    │ LoginController     │    │
│  │ MenuView            │    │    │ DashboardController │    │
│  │ CustomerView        │    │    │ CustomerFormCtrl    │    │
│  │ AccountView         │    │    │ AccountFormCtrl     │    │
│  │ TransactionView     │    │    │ TransactionFormCtrl │    │
│  └─────────────────────┘    │    └─────────────────────┘    │
├─────────────────────────────┴───────────────────────────────┤
│                      SERVICE LAYER                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ BankService (Facade + Singleton)                    │    │
│  │ ├── AuthenticationService                           │    │
│  │ ├── AuthorizationService                            │    │
│  │ ├── AccountService                                  │    │
│  │ └── TransactionService                              │    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                    REPOSITORY LAYER                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │CustomerRepository │ AccountRepository │ TxRepository│    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                      DOMAIN LAYER                           │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ Person ← Customer, Employee                         │    │
│  │ Account ← SavingsAccount, CurrentAccount            │    │
│  │ Transaction, TransactionType, AccountType           │    │
│  │ EmployeeRole                                        │    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                      UTILITY LAYER                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ NationalIdValidator │ AccountValidator │ IdGenerator│    │
│  │ NumberFormatter │ TransactionPrinter                │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## 📚 Documentation

### Architecture & Design

- **[PostgreSQL JDBC Migration Plan](docs/PostgreSQL_JDBC_Plan.md)** — Markdown source of record for the migration from in-memory data structures to PostgreSQL via JDBC (supersedes any prior docx drafts)

### Database Persistence Layer

#### Transformation from In-Memory to PostgreSQL (v2.0.0)

**Overview:**

The system evolved from a prototype in-memory architecture to production-grade database persistence without breaking changes to the service and presentation layers. This demonstrates the power of clean architecture and the Repository pattern.

**Phase 1: In-Memory Prototype (v1.0.0)**
```
Presentation Layer → Service Layer → Repository Layer (Collections)
                                     ├── HashMap<String, Customer>
                                     ├── HashMap<String, Account>
                                     └── ArrayList<Transaction>
                                     (Lost on restart)
```

**Phase 2: PostgreSQL Persistence (v2.0.0)** ✅ **CURRENT**
```
Presentation Layer → Service Layer → Repository Layer (JDBC)
                                     ├── PostgreSQL + JDBC + HikariCP
                                     ├── Connection Pooling (10 max connections)
                                     ├── Full ACID Compliance
                                     └── Data Persistence (Permanent)
```

**Key Architecture Decision:** Only the Repository Layer implementation changed. The Service Layer interfaces remained identical, ensuring zero impact on business logic, controllers, and views.

#### Migration Implementation

| Aspect | In-Memory (v1.0) | PostgreSQL (v2.0) | Benefit |
|--------|------------------|-------------------|---------|
| **Storage** | JVM Heap (HashMap/ArrayList) | PostgreSQL 15+ | Durability & Scalability |
| **Connection** | N/A | JDBC + HikariCP | Thread-safe pooling |
| **Constraints** | Java validation only | Database + Java | Data integrity enforcement |
| **Concurrency** | Not thread-safe | ACID transactions | Safe multi-session access |
| **Queries** | Manual iteration (O(n)) | SQL with indexes | Fast lookups & filtering |
| **Audit Trail** | Volatile, in-memory | Permanent, timestamped | Complete compliance |
| **Scalability** | Limited by RAM | Scales to millions | Enterprise-grade |

#### Repository Layer Architecture

```java
// PUBLIC INTERFACE (Unchanged)
public interface ICustomerRepository {
    void save(Customer customer) throws DuplicateNationalIdException;
    Customer findBySystemId(String systemId);
    Customer findByNationalId(String nationalId);
    // ... unchanged methods
}

// v1.0 IMPLEMENTATION
public class CustomerRepository implements ICustomerRepository {
    private static final Map<String, Customer> customers = new HashMap<>();
    // HashMap-based storage
}

// v2.0 IMPLEMENTATION
public class CustomerRepository implements ICustomerRepository {
    private static final HikariDataSource dataSource = setupPooling();
    // JDBC-based storage with same public interface
}
```

**Key Benefits**

- ✅ **Data Durability** — All customer, account, and transaction records persist across application restarts
- ✅ **Concurrent Access** — Multi-session support with enterprise-grade connection pooling (HikariCP 5.1.0)
- ✅ **Database Constraints** — Unique key enforcement (National ID, account numbers) enforced at the database layer
- ✅ **Query Flexibility** — JDBC enables advanced filtering, pagination, sorting, and reporting capabilities
- ✅ **Audit Trail** — Complete and immutable transaction history maintained indefinitely with timestamps
- ✅ **Production-Ready** — ACID transactions, data integrity, and enterprise compliance
- ✅ **Backward Compatible** — Zero changes to public repository interfaces; service layer workflows unaffected
- ✅ **Zero Service Layer Changes** — Business logic, validation, and workflows remain unchanged

#### Database Configuration

Located in `com.finance.bank.config.DatabaseConfig`:
- **JDBC URL:** `jdbc:postgresql://localhost:5432/finance_bank`
- **Connection Pool:** HikariCP (5.1.0)
  - **Maximum Connections:** 10
  - **Minimum Idle Connections:** 2
  - **Connection Timeout:** 30 seconds
- **Database Driver:** PostgreSQL JDBC Driver (42.7.3)
- **Logging:** SLF4J + Simple Logger (1.7.36)

#### Schema & Initialization

Full database schema (DDL scripts, table definitions, constraints, indexes) is documented in the **[PostgreSQL JDBC Plan](docs/PostgreSQL_JDBC_Plan.md)**.

---

## 📌 Documentation

- **[PostgreSQL JDBC Plan](docs/PostgreSQL_JDBC_Plan.md)** — Complete database migration documentation with schema design, JDBC integration strategy, HikariCP connection pooling configuration, implementation guide, and deployment steps

---

## 📂 Project Structure

```
banking-system/
│
├── pom.xml                          # Maven configuration
├── README.md                        # This file
│
├── docs/                            # Documentation
│   └── PostgreSQL_JDBC_Plan.md      # Database migration & JDBC integration plan
│
├── exports/                         # CSV export output directory
│   ├── transactions_customer_*.csv
│   └── transactions_all_*.csv
│
├── src/
│   ├── main/
│   │   ├── java/com/finance/bank/
│   │   │   │
│   │   │   ├── app/                 # Entry Points
│   │   │   │   ├── BankEmployeeCLI.java
│   │   │   │   └── MainApp.java
│   │   │   │
│   │   │   ├── model/               # Domain Models
│   │   │   │   ├── Person.java
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Employee.java
│   │   │   │   ├── EmployeeRole.java
│   │   │   │   ├── Account.java
│   │   │   │   ├── SavingsAccount.java
│   │   │   │   ├── CurrentAccount.java
│   │   │   │   ├── AccountType.java
│   │   │   │   ├── Transaction.java
│   │   │   │   └── TransactionType.java
│   │   │   │
│   │   │   ├── repository/          # Data Access Layer
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── AccountRepository.java
│   │   │   │   └── TransactionRepository.java
│   │   │   │
│   │   │   ├── service/             # Business Logic
│   │   │   │   ├── BankService.java
│   │   │   │   ├── AuthenticationService.java
│   │   │   │   ├── AuthorizationService.java
│   │   │   │   ├── AccountService.java
│   │   │   │   └── TransactionService.java
│   │   │   │
│   │   │   ├── exception/           # Custom Exceptions
│   │   │   │   ├── InvalidNationalIdException.java
│   │   │   │   ├── DuplicateNationalIdException.java
│   │   │   │   ├── InvalidAccountException.java
│   │   │   │   ├── DuplicateAccountException.java
│   │   │   │   ├── InvalidAmountException.java
│   │   │   │   ├── InsufficientAmountException.java
│   │   │   │   ├── AuthenticationException.java
│   │   │   │   ├── UnauthorizedException.java
│   │   │   │   ├── AccessDeniedException.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   │
│   │   │   ├── util/                # Utilities
│   │   │   │   ├── NationalIdValidator.java
│   │   │   │   ├── AccountValidator.java
│   │   │   │   ├── IdGenerator.java
│   │   │   │   ├── NumberFormatter.java
│   │   │   │   └── TransactionPrinter.java
│   │   │   │
│   │   │   ├── view/                # CLI Views
│   │   │   │   ├── LoginView.java
│   │   │   │   ├── MenuView.java
│   │   │   │   ├── CustomerView.java
│   │   │   │   ├── AccountView.java
│   │   │   │   ├── TransactionView.java
│   │   │   │   └── InputValidator.java
│   │   │   │
│   │   │   └── presentation/        # JavaFX Layer
│   │   │       ├── controllers/
│   │   │       │   ├── LoginController.java
│   │   │       │   ├── DashboardController.java
│   │   │       │   ├── DashboardHomeController.java
│   │   │       │   ├── CustomerFormController.java
│   │   │       │   ├── CustomerListController.java
│   │   │       │   ├── AccountFormController.java
│   │   │       │   ├── AccountListController.java
│   │   │       │   ├── TransactionFormController.java
│   │   │       │   ├── TransactionHistoryController.java
│   │   │       │   └── EmployeeAware.java
│   │   │       └── util/
│   │   │           ├── NavigationManager.java
│   │   │           ├── SessionManager.java
│   │   │           └── AlertHelper.java
│   │   │
│   │   └── resources/com/finance/bank/presentation/
│   │       ├── views/               # FXML Files
│   │       │   ├── login.fxml
│   │       │   ├── dashboard.fxml
│   │       │   ├── dashboard_home.fxml
│   │       │   ├── customer_form.fxml
│   │       │   ├── customer_list.fxml
│   │       │   ├── account_form.fxml
│   │       │   ├── account_list.fxml
│   │       │   ├── transaction_form.fxml
│   │       │   └── transaction_history.fxml
│   │       └── css/
│   │           └── bank-theme.css   # Custom Theme
│   │
│   └── test/java/com/finance/bank/test/
│       └── BankingSystemTest.java   # JUnit 5 Tests
│
└── target/                          # Build output
    └── banking-system-2.0.0.jar
```

---

## ❗ Custom Exceptions

| Exception | Scenario |
|-----------|----------|
| `InvalidNationalIdException` | National ID format/validation failed |
| `DuplicateNationalIdException` | Customer already exists |
| `InvalidAccountException` | Account validation failed |
| `DuplicateAccountException` | Account number already exists |
| `InvalidAmountException` | Amount ≤ 0 or invalid format |
| `InsufficientAmountException` | Balance/overdraft limit exceeded |
| `AuthenticationException` | Login credentials invalid |
| `UnauthorizedException` | Employee lacks permission |
| `AccessDeniedException` | Access to resource denied |
| `ResourceNotFoundException` | Requested resource not found |

---

## 🧠 Design Patterns

| Pattern | Implementation | Purpose |
|---------|----------------|---------|
| **Singleton** | `BankService` | Single point of access to business logic |
| **Facade** | `BankService` | Simplified API for complex subsystems |
| **Repository** | `*Repository` classes | Abstract data storage operations |
| **MVC** | JavaFX Controllers + FXML | Separation of concerns in GUI |
| **Strategy** | `Account.withdraw()` | Different withdrawal rules per account type |
| **Template Method** | `Account` abstract class | Common behavior with customization points |
| **Factory** | `IdGenerator` | Centralized ID generation |
| **Observer** | JavaFX Property Bindings | Real-time UI updates |

---

## 🔒 Security Features

| Feature | Implementation |
|---------|----------------|
| **Role-Based Access Control** | `AuthorizationService` checks on every operation |
| **Immutable Transactions** | All `Transaction` fields are `final` |
| **Masked Account Numbers** | Display as `XXXXXXXXXXXX3456` |
| **Input Validation** | Custom validators for all user input |
| **Exception-Driven Security** | Custom exceptions for all error scenarios |
| **Audit Trail** | Every transaction records employee details |
| **Session Management** | `SessionManager` for secure employee sessions |
| **Defensive Copying** | Unmodifiable collections returned from repositories |

---

## 🪟 JavaFX Desktop Application

### Screenshots & Features

**🔑 Login Screen**
- Employee authentication
- Role detection and display
- Secure session initialization

**📊 Dashboard**
- Role-based navigation sidebar
- Dynamic content loading
- Employee info display
- Quick action buttons

**👤 Customer Management**
- Create customer form with validation
- Customer list with search
- Account count per customer

**💳 Account Management**
- Open Savings/Current accounts
- Configurable overdraft limits
- Account list with filtering

**💰 Transactions**
- Deposit/Withdraw forms
- **Real-time fee calculation**
- **Dynamic fee information card**
- Transaction receipt display
- Transaction history with filters
- Export to CSV

### UI Components

- Custom CSS theme (`bank-theme.css`)
- Responsive sidebar navigation
- Alert system for feedback
- Form validation with visual feedback
- Data tables with sorting
- Masked sensitive data display

---

## 🖥️ CLI Application

Complete console-based workflow:

```
┌─────────────────────────────────────────┐
│           FINANCE BANK CLI              │
├─────────────────────────────────────────┤
│  1. Login                               │
│  2. Create Customer                     │
│  3. Open Account                        │
│  4. Deposit                             │
│  5. Withdraw                            │
│  6. Transaction History                 │
│  7. Export CSV                          │
│  8. Logout                              │
│  0. Exit                                │
└─────────────────────────────────────────┘
```

---

## 🧪 Automated Testing

### Test Coverage

| Category | Tests |
|----------|-------|
| **Authentication** | Login success/failure, invalid credentials |
| **Authorization** | Role-based access, permission denied |
| **Customer** | Creation, validation, duplicates |
| **Account** | Opening, validation, types |
| **Transactions** | Deposit, withdraw, fees, overdraft |
| **Exceptions** | All custom exception scenarios |

### Running Tests

```bash
# Run all tests
mvn test

# Run with verbose output
mvn test -Dtest=BankingSystemTest

# Run specific test method
mvn test -Dtest=BankingSystemTest#testDeposit
```

### Test Isolation

```java
@BeforeEach
void setUp() {
    BankService.getInstance().reset();  // Clean state for each test
}
```

---

## 🚀 Installation

### Prerequisites

- **Java 17** or higher
- **Maven 3.x**
- **JavaFX SDK 21** (handled by Maven)

### Clone & Build

```bash
# Clone repository
git clone https://github.com/omar-abdullah-dev/Core-Banking-Backoffice-Java.git
cd BankSystemWIthExceptions

# Build project
mvn clean install

# Run tests
mvn test
```

---

## 🎮 How to Run

### JavaFX Desktop Application (Recommended)

```bash
mvn javafx:run
```

### CLI Application

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.finance.bank.app.BankEmployeeCLI"

# Or run directly from IDE
# Main class: com.finance.bank.app.BankEmployeeCLI
```

### Default Test Credentials

| Username | Password | Role |
|----------|----------|------|
| `ahmed` | `ahmedPass!` | Manager |
| `mohamed` | `mohamedPass!` | Teller |
| `omar` | `omarPass!` | CS |

---

## 📊 Example Workflow

```
1. Login as Manager (ahmed/ahmedPass!)
2. Create Customer (Enter valid Egyptian National ID)
3. Open Savings Account for customer
4. Deposit EGP 10,000
5. Withdraw EGP 1,000 (Fee: EGP 10, Total: EGP 1,010)
6. View Transaction History
7. Export to CSV
8. Logout
```

---

## 📌 Project Status

| Component | Status |
|-----------|--------|
| CLI Application | ✅ Complete |
| JavaFX GUI | ✅ Complete |
| Automated Tests | ✅ 57 Passing |
| CSV Export | ✅ Complete |
| Documentation | ✅ Complete |
| Code Quality | ✅ Production-Ready |

---

## 🔮 Future Improvements

- [x] **Database Persistence** — ✅ **COMPLETED (v2.0.0)** — Fully migrated to PostgreSQL with JDBC + HikariCP. See [PostgreSQL JDBC Plan](docs/PostgreSQL_JDBC_Plan.md) for implementation details.
- [ ] **Password Hashing** — BCrypt encryption for employee credentials
- [ ] **Spring Boot Integration** — Migrate to Spring Framework for enhanced features
- [ ] **RESTful Web API** — Spring Boot REST endpoints for mobile/external clients
- [ ] **Web Frontend** — React/Angular customer portal
- [ ] **Microservices Architecture** — Service decomposition for scalability
- [ ] **Advanced Logging** — Logback configuration with rolling logs and levels
- [ ] **Docker Containerization** — Dockerfile and docker-compose for deployment
- [ ] **CI/CD Pipeline** — GitHub Actions for automated builds, tests, and deployments

---

## 📄 License

This project is open-source and intended for **educational purposes**.

---

## 👨‍💻 Author

**Omar Abdullah Moharam**

[![GitHub](https://img.shields.io/badge/GitHub-omar--abdullah--dev-black?style=flat-square&logo=github)](https://github.com/omar-abdullah-dev)

---

<div align="center">

**Built with ❤️ to demonstrate clean Java architecture, OOP principles, and production-quality code.**

</div>
