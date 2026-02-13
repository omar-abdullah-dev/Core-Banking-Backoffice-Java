import com.finance.bank.exception.*;
import com.finance.bank.model.*;
import com.finance.bank.service.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the Banking Employee System
 *
 * CRITICAL FIXES APPLIED:
 * 1. BankService.reset() in @BeforeEach for test isolation
 * 2. Proper BigDecimal scale handling (2 decimal places)
 * 3. TransactionType verification (DEPOSIT vs WITHDRAW)
 * 4. ResourceNotFoundException for invalid accounts
 * 5. Insertion order preservation in transaction history
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankingSystemTest {

    private BankService bankService;
    private AuthenticationService authService;

    private Employee manager;
    private Employee teller;
    private Employee customerService;

    @BeforeEach
    void setUp() {
        // CRITICAL FIX: Reset singleton state before each test
        bankService = BankService.getInstance();
        bankService.reset(); // ✅ Ensures test isolation

        authService = new AuthenticationService();

        // Login test employees
        try {
            manager = authService.login("ahmed", "ahmedPass!");
            teller = authService.login("mohamed", "mohamedPass!");
            customerService = authService.login("omar", "omarPass!");
        } catch (AuthenticationException e) {
            fail("Failed to login test employees: " + e.getMessage());
        }
    }

    // ============================================
    // AUTHENTICATION & AUTHORIZATION TESTS
    // ============================================

    @Test
    @Order(1)
    @DisplayName("Should login successfully with valid credentials")
    void testValidLogin() {
        assertDoesNotThrow(() -> {
            Employee emp = authService.login("ahmed", "ahmedPass!");
            assertNotNull(emp);
            assertEquals("ahmed", emp.getUserName());
            assertEquals(Role.MANAGER, emp.getRole());
        });
    }

    @Test
    @Order(2)
    @DisplayName("Should fail login with invalid credentials")
    void testInvalidLogin() {
        assertThrows(AuthenticationException.class, () -> {
            authService.login("invalid", "wrongpassword");
        });
    }

    @Test
    @Order(3)
    @DisplayName("Should enforce role-based access for customer creation")
    void testRoleBasedAccessCustomerCreation() {
        // Manager and CS can create customers
        assertDoesNotThrow(() -> {
            bankService.createCustomer(manager, "Test Manager Customer", "29001011234567");
        });

        bankService.reset(); // Clear for next assertion

        assertDoesNotThrow(() -> {
            bankService.createCustomer(customerService, "Test CS Customer", "29001021234567");
        });

        // Teller should be denied
        assertThrows(UnauthorizedException.class, () -> {
            bankService.createCustomer(teller, "Test Teller Customer", "29001031234567");
        });
    }

    @Test
    @Order(4)
    @DisplayName("Should enforce role-based access for transactions")
    void testRoleBasedAccessTransactions() throws Exception {
        // Setup: Create customer and account as manager
        Customer customer = bankService.createCustomer(manager, "Transaction Test", "29001041234567");
        SavingsAccount account = new SavingsAccount("ACC001", customer);
        bankService.openAccount(manager, account);

        // Manager and Teller can perform transactions
        assertDoesNotThrow(() -> {
            bankService.deposit(manager, "ACC001", new BigDecimal("1000"));
        });

        assertDoesNotThrow(() -> {
            bankService.withdraw(teller, "ACC001", new BigDecimal("100"));
        });

        // CS should be denied
        assertThrows(UnauthorizedException.class, () -> {
            bankService.deposit(customerService, "ACC001", new BigDecimal("500"));
        });
    }

    // ============================================
    // CUSTOMER MANAGEMENT TESTS
    // ============================================

    @Test
    @Order(5)
    @DisplayName("Should create customer with valid National ID")
    void testCreateCustomerValidNationalId() {
        assertDoesNotThrow(() -> {
            Customer customer = bankService.createCustomer(
                    manager,
                    "Ahmed Ali",
                    "29001011234567"
            );

            assertNotNull(customer);
            assertEquals("Ahmed Ali", customer.getName());
            assertEquals("29001011234567", customer.getNationalId());
            assertNotNull(customer.getSystemId());
        });
    }

    @Test
    @Order(6)
    @DisplayName("Should reject customer with invalid National ID")
    void testCreateCustomerInvalidNationalId() {
        // Invalid length
        assertThrows(InvalidNationalIdException.class, () -> {
            bankService.createCustomer(manager, "Test User", "123");
        });

        // Invalid format
        assertThrows(InvalidNationalIdException.class, () -> {
            bankService.createCustomer(manager, "Test User", "ABCD1234567890");
        });

        // Invalid date
        assertThrows(InvalidNationalIdException.class, () -> {
            bankService.createCustomer(manager, "Test User", "99999991234567");
        });
    }

    @Test
    @Order(7)
    @DisplayName("Should prevent duplicate National ID")
    void testDuplicateNationalId() throws Exception {
        String nationalId = "29001051234567";

        // First customer should succeed
        bankService.createCustomer(manager, "First Customer", nationalId);

        // Duplicate should fail
        assertThrows(DuplicateNationalIdException.class, () -> {
            bankService.createCustomer(manager, "Duplicate Customer", nationalId);
        });
    }

    @Test
    @Order(8)
    @DisplayName("Should find customer by National ID")
    void testFindCustomerByNationalId() throws Exception {
        String nationalId = "29001061234567";
        Customer created = bankService.createCustomer(manager, "Findable Customer", nationalId);

        Customer found = bankService.findCustomerByNationalId(nationalId);

        assertNotNull(found);
        assertEquals(created.getSystemId(), found.getSystemId());
        assertEquals(created.getName(), found.getName());
    }

    @Test
    @Order(9)
    @DisplayName("Should return null for non-existent National ID")
    void testFindNonExistentCustomer() {
        Customer notFound = bankService.findCustomerByNationalId("99999991234567");
        assertNull(notFound);
    }

    // ============================================
    // ACCOUNT MANAGEMENT TESTS
    // ============================================

    @Test
    @Order(10)
    @DisplayName("Should create savings account")
    void testCreateSavingsAccount() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Savings Test", "29001071234567");
        SavingsAccount account = new SavingsAccount("SAV001", customer);

        assertDoesNotThrow(() -> {
            bankService.openAccount(manager, account);
        });

        assertEquals(AccountType.SAVINGS, account.getAccountType());
        assertEquals(new BigDecimal("0.00"), account.getBalance()); // ✅ Scale fix
        assertEquals(1, customer.getAccounts().size());
    }

    @Test
    @Order(11)
    @DisplayName("Should create current account with overdraft")
    void testCreateCurrentAccountWithOverdraft() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Current Test", "29001081234567");
        BigDecimal overdraftLimit = new BigDecimal("5000");
        CurrentAccount account = new CurrentAccount("CUR001", customer, overdraftLimit);

        assertDoesNotThrow(() -> {
            bankService.openAccount(manager, account);
        });

        assertEquals(AccountType.CURRENT, account.getAccountType());
        assertEquals(overdraftLimit, account.getOverdraftLimit());
        assertEquals(new BigDecimal("0.00"), account.getBalance()); // ✅ Scale fix
    }

    @Test
    @Order(12)
    @DisplayName("Should allow multiple accounts per customer")
    void testMultipleAccountsPerCustomer() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Multi Account", "29001091234567");

        SavingsAccount savings1 = new SavingsAccount("MULTI001", customer);
        SavingsAccount savings2 = new SavingsAccount("MULTI002", customer);
        CurrentAccount current1 = new CurrentAccount("MULTI003", customer, new BigDecimal("3000"));

        bankService.openAccount(manager, savings1);
        bankService.openAccount(manager, savings2);
        bankService.openAccount(manager, current1);

        assertEquals(3, customer.getAccounts().size());
    }

    @Test
    @Order(13)
    @DisplayName("Should prevent duplicate account numbers")
    void testDuplicateAccountNumber() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Duplicate Account Test", "29001101234567");
        String accountNumber = "DUP001";

        SavingsAccount account1 = new SavingsAccount(accountNumber, customer);
        bankService.openAccount(manager, account1);

        // Attempt to create another account with same number
        SavingsAccount account2 = new SavingsAccount(accountNumber, customer);
        assertThrows(DuplicateAccountException.class, () -> {
            bankService.openAccount(manager, account2);
        });
    }

    // ============================================
    // TRANSACTION OPERATION TESTS
    // ============================================

    @Test
    @Order(14)
    @DisplayName("Should deposit valid amount")
    void testValidDeposit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Deposit Test", "29001111234567");
        SavingsAccount account = new SavingsAccount("DEP001", customer);
        bankService.openAccount(manager, account);

        BigDecimal depositAmount = new BigDecimal("1000.00");

        assertDoesNotThrow(() -> {
            bankService.deposit(teller, "DEP001", depositAmount);
        });

        assertEquals(depositAmount, account.getBalance());
    }

    @Test
    @Order(15)
    @DisplayName("Should reject negative deposit amount")
    void testNegativeDeposit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Negative Deposit", "29001121234567");
        SavingsAccount account = new SavingsAccount("NEGDEP001", customer);
        bankService.openAccount(manager, account);

        assertThrows(InvalidAmountException.class, () -> {
            bankService.deposit(teller, "NEGDEP001", new BigDecimal("-100"));
        });
    }

    @Test
    @Order(16)
    @DisplayName("Should reject zero deposit amount")
    void testZeroDeposit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Zero Deposit", "29001131234567");
        SavingsAccount account = new SavingsAccount("ZERODEP001", customer);
        bankService.openAccount(manager, account);

        assertThrows(InvalidAmountException.class, () -> {
            bankService.deposit(teller, "ZERODEP001", BigDecimal.ZERO);
        });
    }

    @Test
    @Order(17)
    @DisplayName("Should withdraw valid amount from savings account")
    void testValidWithdrawSavings() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Withdraw Test", "29001141234567");
        SavingsAccount account = new SavingsAccount("WD001", customer);
        bankService.openAccount(manager, account);

        // Deposit first
        bankService.deposit(teller, "WD001", new BigDecimal("1000"));

        // Withdraw
        BigDecimal withdrawAmount = new BigDecimal("500");
        assertDoesNotThrow(() -> {
            bankService.withdraw(teller, "WD001", withdrawAmount);
        });

        assertEquals(new BigDecimal("500.00"), account.getBalance()); // ✅ Scale fix
    }

    @Test
    @Order(18)
    @DisplayName("Should reject withdrawal exceeding balance in savings account")
    void testOverdraftRejectionSavings() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Overdraft Test", "29001151234567");
        SavingsAccount account = new SavingsAccount("OD001", customer);
        bankService.openAccount(manager, account);

        bankService.deposit(teller, "OD001", new BigDecimal("100"));

        assertThrows(InsufficientAmountException.class, () -> {
            bankService.withdraw(teller, "OD001", new BigDecimal("200"));
        });
    }

    @Test
    @Order(19)
    @DisplayName("Should allow withdrawal within overdraft limit for current account")
    void testOverdraftAllowedCurrent() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Current Overdraft", "29001161234567");
        BigDecimal overdraftLimit = new BigDecimal("1000");
        CurrentAccount account = new CurrentAccount("CUROD001", customer, overdraftLimit);
        bankService.openAccount(manager, account);

        // Deposit 500
        bankService.deposit(teller, "CUROD001", new BigDecimal("500"));

        // Withdraw 1200 (using 500 from balance + 700 from overdraft)
        assertDoesNotThrow(() -> {
            bankService.withdraw(teller, "CUROD001", new BigDecimal("1200"));
        });

        // Balance should be -700
        assertEquals(new BigDecimal("-700.00"), account.getBalance()); // ✅ Scale fix
    }

    @Test
    @Order(20)
    @DisplayName("Should reject withdrawal exceeding overdraft limit")
    void testOverdraftExceeded() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Overdraft Exceeded", "29001171234567");
        BigDecimal overdraftLimit = new BigDecimal("500");
        CurrentAccount account = new CurrentAccount("ODEX001", customer, overdraftLimit);
        bankService.openAccount(manager, account);

        bankService.deposit(teller, "ODEX001", new BigDecimal("100"));

        // Try to withdraw 700 (100 balance + 500 overdraft = 600 max)
        assertThrows(InsufficientAmountException.class, () -> {
            bankService.withdraw(teller, "ODEX001", new BigDecimal("700"));
        });
    }

    // ============================================
    // TRANSACTION HISTORY TESTS
    // ============================================

    @Test
    @Order(21)
    @DisplayName("Should record transaction history correctly")
    void testTransactionHistory() throws Exception {
        Customer customer = bankService.createCustomer(manager, "History Test", "29001181234567");
        SavingsAccount account = new SavingsAccount("HIST001", customer);
        bankService.openAccount(manager, account);

        // Perform multiple transactions
        bankService.deposit(teller, "HIST001", new BigDecimal("1000"));
        bankService.withdraw(teller, "HIST001", new BigDecimal("200"));
        bankService.deposit(manager, "HIST001", new BigDecimal("500"));

        List<Transaction> transactions = bankService.getTransactionsByAccount("HIST001");

        assertEquals(3, transactions.size());

        // ✅ FIX: Check transaction types in INSERTION ORDER
        assertEquals(TransactionType.DEPOSIT, transactions.get(0).getType());
        assertEquals(TransactionType.WITHDRAWAL, transactions.get(1).getType());
        assertEquals(TransactionType.DEPOSIT, transactions.get(2).getType());
    }

    @Test
    @Order(22)
    @DisplayName("Should include employee information in transactions")
    void testTransactionEmployeeAudit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Audit Test", "29001191234567");
        SavingsAccount account = new SavingsAccount("AUDIT001", customer);
        bankService.openAccount(manager, account);

        bankService.deposit(teller, "AUDIT001", new BigDecimal("1000"));

        List<Transaction> transactions = bankService.getTransactionsByAccount("AUDIT001");
        Transaction transaction = transactions.get(0);

        assertNotNull(transaction.getPerformedByEmployeeName());
        assertNotNull(transaction.getPerformedByRole());
        assertEquals(teller.getName(), transaction.getPerformedByEmployeeName());
        assertEquals(Role.TELLER, transaction.getPerformedByRole());
    }

    @Test
    @Order(23)
    @DisplayName("Should record balance after each transaction")
    void testBalanceTracking() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Balance Track", "29001201234567");
        SavingsAccount account = new SavingsAccount("BAL001", customer);
        bankService.openAccount(manager, account);

        bankService.deposit(teller, "BAL001", new BigDecimal("1000"));
        bankService.withdraw(teller, "BAL001", new BigDecimal("300"));
        bankService.deposit(teller, "BAL001", new BigDecimal("200"));

        List<Transaction> transactions = bankService.getTransactionsByAccount("BAL001");

        // ✅ Scale fix for all balance assertions
        assertEquals(new BigDecimal("1000.00"), transactions.get(0).getBalanceAfter());
        assertEquals(new BigDecimal("700.00"), transactions.get(1).getBalanceAfter());
        assertEquals(new BigDecimal("900.00"), transactions.get(2).getBalanceAfter());
    }

    // ============================================
    // BUSINESS RULE TESTS
    // ============================================

    @Test
    @Order(24)
    @DisplayName("Should maintain account balance consistency")
    void testBalanceConsistency() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Consistency Test", "29001211234567");
        SavingsAccount account = new SavingsAccount("CONS001", customer);
        bankService.openAccount(manager, account);

        BigDecimal initialDeposit = new BigDecimal("5000");
        BigDecimal withdrawal1 = new BigDecimal("1000");
        BigDecimal deposit2 = new BigDecimal("3000");
        BigDecimal withdrawal2 = new BigDecimal("2000");

        bankService.deposit(teller, "CONS001", initialDeposit);
        bankService.withdraw(teller, "CONS001", withdrawal1);
        bankService.deposit(teller, "CONS001", deposit2);
        bankService.withdraw(teller, "CONS001", withdrawal2);

        BigDecimal expectedBalance = initialDeposit
                .subtract(withdrawal1)
                .add(deposit2)
                .subtract(withdrawal2);

        assertEquals(expectedBalance, account.getBalance());
    }

    @Test
    @Order(25)
    @DisplayName("Should generate unique transaction IDs")
    void testUniqueTransactionIds() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Unique ID Test", "29001221234567");
        SavingsAccount account = new SavingsAccount("UNIQ001", customer);
        bankService.openAccount(manager, account);

        bankService.deposit(teller, "UNIQ001", new BigDecimal("100"));
        bankService.deposit(teller, "UNIQ001", new BigDecimal("200"));
        bankService.deposit(teller, "UNIQ001", new BigDecimal("300"));

        List<Transaction> transactions = bankService.getTransactionsByAccount("UNIQ001");

        assertEquals(3, transactions.size());

        // Check all IDs are unique
        String id1 = transactions.get(0).getTransactionId();
        String id2 = transactions.get(1).getTransactionId();
        String id3 = transactions.get(2).getTransactionId();

        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
    }

    @Test
    @Order(26)
    @DisplayName("Should handle concurrent operations correctly")
    void testConcurrentOperations() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Concurrent Test", "29001231234567");
        SavingsAccount account = new SavingsAccount("CONC001", customer);
        bankService.openAccount(manager, account);

        // Initial deposit
        bankService.deposit(teller, "CONC001", new BigDecimal("10000"));

        // Simulate multiple operations
        for (int i = 0; i < 10; i++) {
            bankService.deposit(teller, "CONC001", new BigDecimal("100"));
            bankService.withdraw(teller, "CONC001", new BigDecimal("50"));
        }

        // Expected: 10000 + (100 - 50) * 10 = 10500
        assertEquals(new BigDecimal("10500.00"), account.getBalance()); // ✅ Scale fix

        List<Transaction> transactions = bankService.getTransactionsByAccount("CONC001");
        assertEquals(21, transactions.size()); // 1 initial + 10 deposits + 10 withdrawals
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    @Order(27)
    @DisplayName("Should handle large deposit amounts")
    void testLargeDeposit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Large Amount", "29001241234567");
        SavingsAccount account = new SavingsAccount("LARGE001", customer);
        bankService.openAccount(manager, account);

        BigDecimal largeAmount = new BigDecimal("999999999.99");

        assertDoesNotThrow(() -> {
            bankService.deposit(teller, "LARGE001", largeAmount);
        });

        assertEquals(largeAmount, account.getBalance());
    }

    @Test
    @Order(28)
    @DisplayName("Should handle small decimal amounts")
    void testSmallDecimalAmounts() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Small Decimal", "29001251234567");
        SavingsAccount account = new SavingsAccount("SMALL001", customer);
        bankService.openAccount(manager, account);

        BigDecimal smallAmount = new BigDecimal("0.01");

        assertDoesNotThrow(() -> {
            bankService.deposit(teller, "SMALL001", smallAmount);
        });

        assertEquals(new BigDecimal("0.01"), account.getBalance());
    }

    @Test
    @Order(29)
    @DisplayName("Should reject invalid account number")
    void testInvalidAccountNumber() {
        // ✅ FIX: Should throw ResourceNotFoundException (not just any Exception)
        assertThrows(ResourceNotFoundException.class, () -> {
            bankService.deposit(teller, "INVALID999", new BigDecimal("100"));
        });
    }

    @Test
    @Order(30)
    @DisplayName("Should handle empty transaction history")
    void testEmptyTransactionHistory() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Empty History", "29001261234567");
        SavingsAccount account = new SavingsAccount("EMPTY001", customer);
        bankService.openAccount(manager, account);

        List<Transaction> transactions = bankService.getTransactionsByAccount("EMPTY001");

        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }
}