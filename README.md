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
    │   └── BankEmployeeCLI.java
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
    │   ├── CustomerService.java
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
Services (Business Logic + Authorization)
        ↓
Repositories (In-Memory Persistence)
        ↓
Domain Models (Account, Customer, Transaction)
```

### Layers

- **Presentation Layer:** `BankEmployeeCLI`
- **Service Layer:** Business logic & authorization
- **Repository Layer:** In-memory data storage
- **Domain Layer:** Core banking models
- **Utility Layer:** Validation, formatting, ID generation, CSV export

### Design Principles Applied

- Separation of concerns
- Encapsulation & abstraction
- Polymorphism
- Defensive copying
- Singleton (`BankService`) as a composition root
- Audit-friendly immutable transactions

---

## 🔐 Authentication & Authorization

- Employee login (username/password)
- Role-based access control:
  - **CS (Customer Service):** Create customers, add accounts
  - **TELLER:** Deposit, withdraw, view transactions
  - **MANAGER:** Full access
- Authorization enforced **inside the service layer**

---

## 👤 Customer Management

- Create customers with **Egyptian National ID validation**
- Prevent duplicate customers
- List customers and account counts

### 🇪🇬 Egyptian National ID Validation

Handled by `NationalIdValidator`:

- Length validation (14 digits)
- Birthdate parsing and validation
- Governorate code validation

---

## 💳 Account Management

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

---

## 💰 Transactions

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

---

## 📜 Transaction History

- Centralized in `TransactionRepository`
- Source of truth (not stored in Account)
- Read-only audit log
- Sorted by timestamp (latest first)
- Displayed in a clean, human-readable CLI format

---

## 📤 CSV Export (Excel-Compatible)

- Export transaction history **per account**
- Files saved under `exports/`
- Append-only (never overwritten)
- Incremental export:
  - Only new transactions are exported
  - Prevents duplicate records
- No external libraries required

### CSV Format

```csv
Type,Amount,BalanceAfter,Timestamp,TransactionId
DEPOSIT,1000.00,1000.00,2024-01-15T10:30:00,TXN001
WITHDRAW,500.00,500.00,2024-01-15T14:20:00,TXN002
```

---

## 🔒 Security & Data Protection

- Account numbers are masked in all outputs
- Internal collections are returned as read-only views
- Transactions are immutable after creation
- Authorization enforced at the service layer

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

## 🧪 Test Coverage (Manual)

- ✅ Login success/failure
- ✅ Role enforcement
- ✅ Create customer (valid/duplicate)
- ✅ Add account (valid/invalid)
- ✅ Deposit/withdraw:
  - Valid
  - Zero/negative
  - Overdraft exceeded
- ✅ Transaction history correctness
- ✅ CSV export (first time/incremental)
- ✅ Logout/Exit

---

## 📌 Project Status

- ✅ Feature-complete
- ✅ Architecturally sound
- ✅ Bug-free (manually tested)
- ✅ Interview-ready

---

## 🔮 Future Improvements

- Database persistence (H2/PostgreSQL/MySQL)
- Password hashing
- Unit & integration testing (JUnit)
- Logging (SLF4J)
- REST API or GUI frontend

---

## 👨‍💻 Author

**Omar Abdullah Moharam**

GitHub: [omarAbdullahMoharam](https://github.com/omarAbdullahMoharam)

---

## 📄 License

Open-source and intended for educational purposes.