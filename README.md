# 🏦 Banking Employee System

A Banking Employee Back-Office System implemented in Java, designed using clean Object-Oriented Programming (OOP) principles and a layered architecture, with strong validation, exception handling, audit logging, automated testing, CSV export, and a full JavaFX Desktop GUI.

This project simulates how bank employees manage customers, accounts, and transactions through:

- 🖥️ **Console Application (CLI)**
- 🪟 **Desktop Application (JavaFX GUI)**
- 🧪 **Full JUnit 5 Test Suite**
- 📤 **CSV Export System**
- 🔐 **Role-Based Access Control**

> ⚠️ **Scope Notice:** This system represents employee-assisted banking operations only.
> No customer self-service, online banking, or external transfers.

---

## 📂 Project Structure
```
exports/

src/
└── main/
    └── java/
        └── com/finance/bank/
            │
            ├── app/
            │   ├── BankEmployeeCLI.java          # CLI entry point
            │   └── MainApp.java                  # JavaFX entry point
            │
            ├── view/                             # CLI Presentation Layer
            │   ├── LoginView.java
            │   ├── MenuView.java
            │   ├── CustomerView.java
            │   ├── AccountView.java
            │   ├── TransactionView.java
            │   └── InputValidator.java
            │
            ├── presentation/                     # JavaFX Presentation Layer
            │   ├── controllers/
            │   │   ├── LoginController.java
            │   │   ├── DashboardController.java
            │   │   ├── DashboardHomeController.java
            │   │   ├── CustomerFormController.java
            │   │   ├── CustomerListController.java
            │   │   ├── AccountFormController.java
            │   │   ├── AccountListController.java
            │   │   ├── TransactionFormController.java
            │   │   └── TransactionHistoryController.java
            │   └── util/
            │       ├── NavigationManager.java
            │       └── AlertHelper.java
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

    └── resources/
        └── com/finance/bank/presentation/
            ├── views/
            │   ├── login.fxml
            │   ├── dashboard.fxml
            │   ├── dashboard_home.fxml
            │   ├── customer_form.fxml
            │   ├── customer_list.fxml
            │   ├── account_form.fxml
            │   ├── account_list.fxml
            │   ├── transaction_form.fxml
            │   └── transaction_history.fxml
            └── css/
                └── bank-theme.css

└── test/
    └── java/
        └── BankingSystemTests.java
```

---

## 🧱 Architecture Overview
```
CLI (Console UI) + JavaFX (Desktop UI)
              ↓
     Presentation Layer
              ↓
   Service Layer (Business Logic)
              ↓
  Repository Layer (In-Memory Storage)
              ↓
    Domain Layer (Core Models)
```

---

## 🪟 JavaFX Desktop Application

A fully functional Desktop GUI built using JavaFX — without modifying any business logic.

**Login Screen** — Authentication, role detection, and secure access.

**Dashboard** — Role-based navigation with dynamic content loading.

**Customer Management** — Create, view, and search customers.

**Account Management** — Open and view accounts.

**Transactions** — Deposit, withdraw, transaction history, and audit view.

**Export** — CSV export.

---

## 🖥️ CLI Application Features

Complete console-based workflow: Login → Customer creation → Account creation → Deposit → Withdraw → Transaction history → CSV Export.

---

## 🔐 Authentication & Authorization

| Role | Permissions |
|------|-------------|
| Customer Service | Create Customer, Open Account |
| Teller | Deposit, Withdraw |
| Manager | Full Access |

Enforced in `AuthorizationService`.

---

## 💰 Transaction System

Each transaction stores: ID, Type, Amount, Balance After, Timestamp, Employee Name, and Employee Role. All transactions are **fully immutable**.

---

## 📤 CSV Export

Export includes: Type, Amount, Balance, Timestamp, Employee, Role, and Transaction ID. Append-only — no duplicates.

---

## 🧪 Automated Testing

Full test suite using **JUnit 5**, covering:

Authentication, Authorization, Customer creation, Account creation, Deposit, Withdraw, Overdraft, Transaction history, and Exception handling.

Uses `BankService.reset()` for test isolation.

---

## 🧠 Design Patterns Used

| Pattern | Usage |
|---------|-------|
| Singleton | BankService |
| MVC | JavaFX |
| Repository | Data Layer |
| Facade | BankService |
| Strategy | Account Types |
| Polymorphism | Withdraw Logic |

---

## 🔒 Security

- Role-based access control
- Immutable transactions
- Masked account numbers
- Exception handling
- Audit logging

---

## 🚀 How to Run

**CLI:**
```bash
# Run:
BankEmployeeCLI.java
```

**JavaFX:**
```bash
mvn javafx:run
```

---

## 📌 Project Status

- ✅ CLI Complete
- ✅ JavaFX Complete
- ✅ Fully Tested
- ✅ Production-Quality Code
- ✅ Portfolio-Level Project

## 🔮 Future Improvements

- Database Integration
- Spring Boot API
- Web Frontend
- Microservices

---

## 👨‍💻 Author

**Omar Abdullah Moharam**  
GitHub: [omar-abdullah-dev](https://github.com/omar-abdullah-dev)