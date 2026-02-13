# Bank Employee CLI - Refactored Architecture

## Overview
This refactoring separates the monolithic `BankEmployeeCLI` class into multiple view classes following the **Separation of Concerns** principle and **MVC-like architecture**.

## Architecture

### Original Structure
```
BankEmployeeCLI.java (700+ lines)
├── Login handling
├── Menu display
├── Customer operations
├── Account operations
├── Transaction operations
├── Input validation
└── Display formatting
```

### Refactored Structure
```
com.finance.bank.app
└── BankEmployeeCLI.java (Main entry point - ~100 lines)

com.finance.bank.view
├── LoginView.java           (Authentication UI)
├── MenuView.java            (Role-based menu display)
├── CustomerView.java        (Customer operations UI)
├── AccountView.java         (Account operations UI)
├── TransactionView.java     (Transaction operations UI)
└── InputValidator.java      (Reusable input validation)
```

## Components

### 1. BankEmployeeCLI (Main Controller)
**Responsibility**: Application orchestration and routing
- Initializes all views
- Manages application lifecycle
- Routes user choices to appropriate views
- Handles logout and exit

**Key Methods**:
- `main()` - Application entry point
- `initializeViews()` - Creates view instances
- `handleMenuChoice()` - Routes menu selections

---

### 2. LoginView
**Responsibility**: Employee authentication UI
- Displays login screen
- Handles login attempts
- Shows success/failure messages

**Key Methods**:
- `handleLogin()` - Login loop until success
- `displayLoginSuccess()` - Success message
- `displayLoginError()` - Error message

---

### 3. MenuView
**Responsibility**: Role-based menu display
- Shows menus based on employee role (CS, TELLER, MANAGER)
- Gets user menu choice
- Different menu options for different roles

**Key Methods**:
- `displayMenu(Employee)` - Shows role-appropriate menu
- `displayCustomerServiceMenu()` - CS options
- `displayTellerMenu()` - Teller options
- `displayManagerMenu()` - Manager (all) options
- `getChoice()` - Reads user input

---

### 4. CustomerView
**Responsibility**: Customer-related operations UI
- Create customer workflow
- Display customer list
- Find customer by National ID

**Key Methods**:
- `handleCreateCustomer(Employee)` - Customer creation flow
- `handleShowCustomers()` - List all customers
- `findCustomerByNationalId()` - Find customer (used by other views)

**Dependencies**:
- `BankService` - Business logic
- `InputValidator` - Input validation

---

### 5. AccountView
**Responsibility**: Account-related operations UI
- Create savings/current accounts
- Display accounts by customer
- Account selection from multiple accounts

**Key Methods**:
- `handleAddAccount(Employee)` - Account creation flow
- `handleShowAccountsByNationalId()` - Display customer accounts
- `chooseAccountFromCustomer(Customer)` - Account selection UI
- `createSavingsAccount()` - Savings account creation
- `createCurrentAccount()` - Current account with overdraft

**Dependencies**:
- `BankService` - Business logic
- `CustomerView` - Find customers
- `InputValidator` - Input validation

---

### 6. TransactionView
**Responsibility**: Transaction operations UI
- Deposit workflow
- Withdrawal workflow
- Transaction history display
- CSV export

**Key Methods**:
- `handleDeposit(Employee)` - Deposit flow
- `handleWithdraw(Employee)` - Withdrawal flow
- `handleTransactionHistory()` - Display transactions
- `handleExportTransactions()` - CSV export
- `displayTransactionHistory()` - Formatted display

**Dependencies**:
- `BankService` - Business logic
- `CustomerView` - Find customers
- `AccountView` - Account selection
- `InputValidator` - Input validation

---

### 7. InputValidator (Utility)
**Responsibility**: Reusable input validation logic
- National ID validation
- BigDecimal (amount) validation
- Integer validation
- String validation
- Confirmation prompts

**Key Methods**:
- `readAndValidateNationalId()` - National ID input
- `readBigDecimal(String)` - Amount input with 'q' to cancel
- `readInteger(String, min, max)` - Bounded integer input
- `readNonEmptyString(String)` - Required string input
- `readConfirmation(String)` - Yes/no prompts

---

## Benefits of Refactoring

### 1. **Separation of Concerns**
- Each view handles one domain (customers, accounts, transactions)
- Input validation separated into utility class
- Business logic stays in service layer

### 2. **Maintainability**
- Smaller, focused classes (~100-200 lines each)
- Easy to locate and fix bugs
- Clear responsibility boundaries

### 3. **Reusability**
- `InputValidator` used across all views
- `CustomerView.findCustomerByNationalId()` used by Account and Transaction views
- `AccountView.chooseAccountFromCustomer()` used by Transaction view

### 4. **Testability**
- Each view can be tested independently
- Mock dependencies easily
- Clear interfaces between components

### 5. **Scalability**
- Easy to add new views (e.g., ReportView, AdminView)
- Can extend functionality without touching other views
- Clear patterns to follow

### 6. **Single Responsibility Principle**
- Each class has one reason to change
- LoginView changes only for authentication UI changes
- TransactionView changes only for transaction UI changes

---

## Data Flow

```
User Input
    ↓
MenuView (get choice)
    ↓
BankEmployeeCLI (route)
    ↓
Specific View (e.g., TransactionView)
    ↓
InputValidator (validate input)
    ↓
BankService (business logic)
    ↓
View (display result)
    ↓
User sees output
```

---

## View Dependencies

```
BankEmployeeCLI
├── LoginView
├── MenuView
├── CustomerView
│   └── InputValidator
├── AccountView
│   ├── CustomerView
│   └── InputValidator
└── TransactionView
    ├── CustomerView
    ├── AccountView
    └── InputValidator
```

---

## Usage Example

### Creating a Customer (CS/Manager)
```
1. User logs in → LoginView.handleLogin()
2. Menu displayed → MenuView.displayMenu()
3. User selects "1" → BankEmployeeCLI routes to CustomerView
4. CustomerView.handleCreateCustomer():
   - Prompts for name
   - Uses InputValidator.readAndValidateNationalId()
   - Calls BankService.createCustomer()
   - Displays success message
```

### Making a Deposit (Teller/Manager)
```
1. User selects "5" → BankEmployeeCLI routes to TransactionView
2. TransactionView.handleDeposit():
   - Uses CustomerView.findCustomerByNationalId()
   - Uses AccountView.chooseAccountFromCustomer()
   - Uses InputValidator.readBigDecimal()
   - Calls BankService.deposit()
   - Displays success with new balance
```

---

## Migration Guide

### Before (Original)
```java
// Everything in one class
public class BankEmployeeCLI {
    private static void handleCreateCustomer(Scanner in) { ... }
    private static void handleDeposit(Scanner in) { ... }
    private static String readAndValidateNationalId(Scanner in) { ... }
    // ... 15+ methods
}
```

### After (Refactored)
```java
// Main controller
public class BankEmployeeCLI {
    private static CustomerView customerView;
    private static TransactionView transactionView;
    
    public static void main(String[] args) {
        initializeViews(scanner);
        // Route to views
    }
}

// Specialized views
public class CustomerView {
    public void handleCreateCustomer(Employee employee) { ... }
}

public class TransactionView {
    public void handleDeposit(Employee employee) { ... }
}

// Shared utilities
public class InputValidator {
    public String readAndValidateNationalId() { ... }
}
```

---

## Package Structure

```
com.finance.bank
├── app
│   └── BankEmployeeCLI.java          (Main entry point)
├── view
│   ├── LoginView.java                (Authentication)
│   ├── MenuView.java                 (Menu display)
│   ├── CustomerView.java             (Customer operations)
│   ├── AccountView.java              (Account operations)
│   ├── TransactionView.java          (Transaction operations)
│   └── InputValidator.java           (Input validation utility)
├── service
│   ├── AuthenticationService.java    (Login logic)
│   └── BankService.java              (Business logic)
├── model
│   ├── Employee.java
│   ├── Customer.java
│   ├── Account.java
│   └── Transaction.java
└── util
    ├── NationalIdValidator.java
    ├── NumberFormatter.java
    └── TransactionPrinter.java
```

---

## Future Enhancements

With this architecture, you can easily add:

1. **New Views**
   - `ReportView` for analytics
   - `AdminView` for system management
   - `AuditView` for compliance

2. **Enhanced Input Validation**
   - Add more validators to `InputValidator`
   - Create specialized validators

3. **Improved Error Handling**
   - Centralized error display
   - Error logging

4. **GUI Migration**
   - Views can be adapted to GUI
   - Business logic unchanged

---

## Best Practices Applied

✅ **Single Responsibility** - Each class has one job  
✅ **DRY (Don't Repeat Yourself)** - Shared logic in InputValidator  
✅ **Dependency Injection** - Views receive dependencies via constructor  
✅ **Encapsulation** - Private helper methods, public interface  
✅ **Loose Coupling** - Views depend on interfaces, not implementations  
✅ **High Cohesion** - Related functionality grouped together  

---

## Testing Strategy

### Unit Testing
```java
// Test individual views
@Test
void testLoginView_validCredentials() {
    // Mock AuthenticationService
    // Test successful login flow
}

@Test
void testInputValidator_invalidNationalId() {
    // Test validation logic
}
```

### Integration Testing
```java
@Test
void testDepositFlow_endToEnd() {
    // Test complete deposit workflow
    // Customer selection → Account selection → Amount input → Deposit
}
```

---

## Conclusion

This refactoring transforms a 700+ line monolithic class into a clean, maintainable, and extensible architecture. Each view is focused, reusable, and testable. The separation of concerns makes the codebase easier to understand, modify, and extend.
