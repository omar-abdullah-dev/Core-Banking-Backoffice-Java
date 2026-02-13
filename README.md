# 🏦 Banking Employee System

A **Banking Employee Back-Office System** implemented in **Java**, designed using **clean Object-Oriented Programming (OOP)** principles and a **layered architecture** with strong validation, exception handling, and audit-friendly design.

This project simulates how **bank employees** manage customers, accounts, and transactions through a **console-based (CLI) application** with **authentication, authorization, transaction history, and CSV export**.

> ⚠️ **Scope Notice**  
> This system represents **employee-assisted banking operations only**.  
> No customer self-service, online banking, or peer-to-peer transfers.

---

## 📂 Project Structure

```
exports/
src/
└── com/finance/bank/
    ├── app/
    │   └── BankEmployeeCLI.java          (Main controller & entry point)
    │
    ├── view/
    │   ├── LoginView.java                (Authentication UI)
    │   ├── MenuView.java                 (Role-based menu display)
    │   ├── CustomerView.java             (Customer operations UI)
    │   ├── AccountView.java              (Account operations UI)
    │   ├── TransactionView.java          (Transaction operations UI)
    │   └── InputValidator.java           (Reusable input validation)
    │
    ├── exception/
    │   ├── AccessDeniedException.java
    │   ├── AuthenticationException.java
    │   ├── DuplicateAccountException.java
    │   ├── DuplicateNationalIdException.java
    │   ├── InsufficientAmountException.java
    │   ├── InvalidAccountException.java
    │   ├── InvalidAmountException.java
    │   ├── InvalidNationalIdException.java
    │   ├── ResourceNotFoundException.java
    │   └── UnauthorizedException.java
    │
    ├── model/
    │   ├── Account.java
    │   ├── AccountType.java
    │   ├── CurrentAccount.java
    │   ├── SavingsAccount.java
    │   ├── Customer.java
    │   ├── Employee.java
    │   ├── Person.java
    │   ├── Role.java
    │   ├── Transaction.java
    │   └── TransactionType.java
    │
    ├── repository/
    │   ├── AccountRepository.java
    │   ├── CustomerRepository.java
    │   └── TransactionRepository.java
    │
    ├── service/
    │   ├── AuthenticationService.java
    │   ├── AuthorizationService.java
    │   ├── AccountService.java
    │   ├── TransactionService.java
    │   └── BankService.java
    │
    └── util/
        ├── AccountValidator.java
        ├── IdGenerator.java
        ├── NationalIdValidator.java
        ├── NumberFormatter.java
        └── TransactionPrinter.java

```

---

## 🧱 Architecture Overview

```
CLI (Presentation)
        ↓
View Layer (UI Logic & Input Validation)
        ↓
Services (Business Logic + Authorization)
        ↓
Repositories (In-Memory Persistence)
        ↓
Domain Models (Account, Customer, Transaction)
```

### Layers

- **Presentation Layer:** `BankEmployeeCLI` (Main controller)
- **View Layer:** Specialized UI components for different domains
  - `LoginView` - Authentication interface
  - `MenuView` - Role-based menu display
  - `CustomerView` - Customer operations UI
  - `AccountView` - Account operations UI
  - `TransactionView` - Transaction operations UI
  - `InputValidator` - Centralized input validation
- **Service Layer:** Business logic & authorization
- **Repository Layer:** In-memory data storage
- **Domain Layer:** Core banking models
- **Utility Layer:** Validation, formatting, ID generation, CSV export

### Design Principles Applied

- **Separation of Concerns** - Each view handles one specific domain
- **Single Responsibility** - Each class has one reason to change
- **DRY (Don't Repeat Yourself)** - Shared logic in `InputValidator`
- **Encapsulation & Abstraction**
- **Polymorphism**
- **Defensive Copying**
- **Dependency Injection** - Views receive dependencies via constructor
- **Singleton Pattern** - `BankService` as a composition root
- **Audit-friendly** - Immutable transactions with employee context

### View Architecture Benefits

✅ **Maintainability** - Each view is ~100-200 lines (vs 700+ in monolithic design)  
✅ **Testability** - Views can be unit tested independently  
✅ **Reusability** - `InputValidator` and helper methods shared across views  
✅ **Extensibility** - Easy to add new views (e.g., `ReportView`, `AdminView`)  
✅ **Clarity** - Clear responsibility boundaries between components

---

## 🔐 Authentication & Authorization

- Employee login (username/password) handled by `LoginView`
- Role-based access control:
  - **CS (Customer Service):** Create customers, add accounts
  - **TELLER:** Deposit, withdraw, view transactions
  - **MANAGER:** Full access
- Authorization enforced **inside the service layer**
- Menu options dynamically displayed based on employee role via `MenuView`

---

## 👤 Customer Management

Handled by `CustomerView`:

- Create customers with **Egyptian National ID validation**
- Prevent duplicate customers
- List customers and account counts
- Find customers by National ID (used by other views)

### 🇪🇬 Egyptian National ID Validation

Handled by `NationalIdValidator` and used through `InputValidator`:

- Length validation (14 digits)
- Birthdate parsing and validation
- Governorate code validation

---

## 💳 Account Management

Handled by `AccountView`:

### Savings Account

- No overdraft
- Withdrawals limited to balance

### Current Account

- Supports overdraft up to a configurable limit

### Features

- Multiple accounts per customer
- Account numbers are:
  - System-generated
  - Masked in output (e.g., `XXXXXXXXXXXX3456`)
- Smart account selection (auto-select if only one account, prompt if multiple)

---

## 💰 Transactions

Handled by `TransactionView`:

### Supported Operations

- **Deposit**
- **Withdraw**

Polymorphic business rules (Savings vs Current)

### Transaction Record

Each operation creates an **immutable Transaction record** containing:

- Transaction type (`DEPOSIT` / `WITHDRAW`)
- Amount
- Balance after operation
- Timestamp
- Unique transaction ID
- Performed-by employee (audit trail)

### Workflow

```
TransactionView
    ↓
CustomerView.findCustomerByNationalId()
    ↓
AccountView.chooseAccountFromCustomer()
    ↓
InputValidator.readBigDecimal()
    ↓
BankService.deposit() / withdraw()
    ↓
Display success with new balance
```

---

## 📜 Transaction History

- Centralized in `TransactionRepository`
- Source of truth (not stored in Account)
- Read-only audit log
- Sorted by timestamp (latest first)
- Displayed in a clean, human-readable CLI format via `TransactionView`
- Shows employee who performed each transaction

---

## 📤 CSV Export (Excel-Compatible)

Managed by `TransactionView` using `TransactionPrinter`:

- Export transaction history **per account**
- Files saved under `exports/`
- Append-only (never overwritten)
- Incremental export:
  - Only new transactions are exported
  - Prevents duplicate records
- Includes employee audit information (name, role)
- No external libraries required

### CSV Format

```csv
Type,Amount,BalanceAfter,Timestamp,TransactionId,PerformedBy,Role
DEPOSIT,1000.00,1000.00,2024-01-15T10:30:00,TXN001,John Smith,TELLER
WITHDRAW,500.00,500.00,2024-01-15T14:20:00,TXN002,Jane Doe,TELLER
```

---

## 🔒 Security & Data Protection

- Account numbers are masked in all outputs
- Internal collections are returned as read-only views
- Transactions are immutable after creation
- Authorization enforced at the service layer
- Employee authentication required for all operations
- Role-based menu prevents unauthorized access attempts

---

## ❗ Custom Exceptions

| Exception | Purpose |
|-----------|---------|
| `AuthenticationException` | Invalid login credentials |
| `UnauthorizedException` | Authenticated but lacks role |
| `AccessDeniedException` | Explicit access denial |
| `DuplicateNationalIdException` | Customer already exists |
| `DuplicateAccountException` | Account number already exists |
| `InvalidNationalIdException` | Invalid Egyptian National ID |
| `InvalidAmountException` | Amount ≤ 0 |
| `InsufficientAmountException` | Balance/overdraft exceeded |
| `InvalidAccountException` | Invalid or null account |
| `ResourceNotFoundException` | Entity not found |

All exceptions are properly caught and displayed to users with meaningful messages.

---

## 🚀 How to Run

### ▶️ Using an IDE

Run:
```
com.finance.bank.app.BankEmployeeCLI
```

### ▶️ Using Terminal

```bash
javac -d bin -sourcepath src src/com/finance/bank/app/BankEmployeeCLI.java
java -cp bin com.finance.bank.app.BankEmployeeCLI
```

**Recommended:** JDK 17+

---

## 🎯 View Layer Components

### LoginView
- Handles employee authentication
- Displays login prompts
- Shows success/error messages
- Ensures login before system access

### MenuView
- Displays role-based menus
- Shows different options for CS, TELLER, MANAGER
- Gets user menu choice
- Clean, organized menu layout

### CustomerView
- Customer creation workflow
- Customer listing
- Customer lookup by National ID
- Shared by other views for customer selection

### AccountView
- Account creation (Savings/Current)
- Account listing by customer
- Smart account selection (auto-select or prompt)
- Shared by TransactionView for account selection

### TransactionView
- Deposit workflow
- Withdrawal workflow
- Transaction history display
- CSV export functionality
- Coordinates with CustomerView and AccountView

### InputValidator
- National ID validation
- Amount validation (BigDecimal)
- Integer validation with bounds
- String validation
- Confirmation prompts
- Centralized input logic used across all views

---

## 🧪 Test Coverage (Manual)

- ✅ Login success/failure
- ✅ Role enforcement (CS, TELLER, MANAGER menus)
- ✅ Create customer (valid/duplicate)
- ✅ Add account (valid/invalid)
- ✅ Deposit/withdraw:
  - Valid transactions
  - Zero/negative amounts
  - Overdraft exceeded
  - Insufficient balance
- ✅ Transaction history correctness
- ✅ CSV export (first time/incremental)
- ✅ Account selection (single/multiple)
- ✅ Employee audit trail in transactions
- ✅ Logout/Exit functionality
- ✅ Input validation (National ID, amounts, choices)

---

## 📌 Project Status

- ✅ Feature-complete
- ✅ Architecturally sound with **view-based separation**
- ✅ Bug-free (manually tested)
- ✅ Clean code with **~100-200 lines per class**
- ✅ Interview-ready
- ✅ **Refactored from monolithic to modular architecture**

---

## 🔮 Future Improvements

### Technical Enhancements
- Database persistence (H2/PostgreSQL/MySQL)
- Password hashing (BCrypt)
- Unit & integration testing (JUnit 5)
- Logging framework (SLF4J + Logback)
- Configuration management (properties/YAML)

### Architecture Evolution
- REST API layer (Spring Boot)
- GUI frontend (JavaFX or web-based)
- Microservices architecture
- Event-driven design

### Feature Additions
- Multi-factor authentication
- Transaction reversal/refund
- Account statements
- Interest calculation
- Account closure
- Customer notifications
- Audit log viewer
- Advanced reporting

---

## 👨‍💻 Author

**Omar Abdullah Moharam**

GitHub: [OmarAbdullahMoharam](https://github.com/omar-abdullah-dev)

---

## 📄 License

Open-source and intended for educational purposes.

---

## 🏗️ Architecture Highlights

This project demonstrates professional software engineering practices:

- **Clean separation of concerns** across layers
- **View-based UI architecture** for maintainability
- **Service layer** for business logic and authorization
- **Repository pattern** for data access
- **Domain-driven design** with rich models
- **Defensive programming** with validation and exception handling
- **Audit trail** with immutable transaction records
- **Role-based access control** throughout the system
- **Reusable components** reducing code duplication
- **Extensible design** supporting future enhancements