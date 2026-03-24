package com.finance.bank.test;

import com.finance.bank.exception.*;
import com.finance.bank.model.*;
import com.finance.bank.service.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * UNIT TEST SUITE  —  BankingSystemTest
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * These are PURE UNIT TESTS.
 * They run against a real PostgreSQL DB (your test DB), NOT in-memory mocks.
 *
 * IMPORTANT — before running this file:
 *   1. Make sure src/test/resources/database.properties points to banking_test
 *   2. Run:  mvn test
 *
 * These tests call bankService.reset() in @BeforeEach which runs:
 *   DELETE FROM transactions → DELETE FROM accounts → DELETE FROM customers
 * on whatever DB is configured in test resources.
 *
 * DO NOT point database.properties at your production DB while running tests.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankingSystemTest {

    private BankService bankService;
    private AuthenticationService authService;

    private Employee manager;
    private Employee teller;
    private Employee customerService;

    // Withdrawal fee constant (1%)
    private static final BigDecimal WITHDRAWAL_FEE_PERCENT = new BigDecimal("0.01");

    @BeforeEach
    void setUp() {
        bankService = BankService.getInstance();

        /*
         * reset() runs DELETE FROM transactions / accounts / customers
         * on the DB configured in src/test/resources/database.properties
         * Make sure that file points to banking_test, not production!
         */
        bankService.reset();

        authService = new AuthenticationService();

        try {
            manager         = authService.login("manager", "manager123");
            teller          = authService.login("teller",  "teller123");
            customerService = authService.login("cs",      "cs123456");
        } catch (AuthenticationException e) {
            fail("Failed to login test employees: " + e.getMessage());
        }
    }

    // ── Helper: valid 16-digit account number with bank prefix ────────────────
    private String generateAccountNumber(int suffix) {
        return String.format("10010001%08d", suffix);
    }

    // ── Helper: total withdrawn = amount + 1% fee ─────────────────────────────
    private BigDecimal calculateTotalWithdrawal(BigDecimal amount) {
        return amount.add(amount.multiply(WITHDRAWAL_FEE_PERCENT));
    }

    // =========================================================================
    // AUTHENTICATION & AUTHORIZATION
    // =========================================================================

    @Test
    @Order(1)
    @DisplayName("Should login successfully with valid credentials")
    void testValidLogin() {
        assertDoesNotThrow(() -> {
            Employee emp = authService.login("manager", "manager123");
            assertNotNull(emp);
            assertEquals("manager",            emp.getUserName());
            assertEquals(EmployeeRole.MANAGER,  emp.getRole());
        });
    }

    @Test
    @Order(2)
    @DisplayName("Should fail login with invalid credentials")
    void testInvalidLogin() {
        assertThrows(AuthenticationException.class,
                () -> authService.login("invalid", "wrongpassword"));
    }

    @Test
    @Order(3)
    @DisplayName("Should enforce role-based access for customer creation")
    void testRoleBasedAccessCustomerCreation() {
        assertDoesNotThrow(() ->
                bankService.createCustomer(manager, "Test Manager Customer", "29001011234567"));

        bankService.reset();   // wipe between sub-scenarios inside the same test

        assertDoesNotThrow(() ->
                bankService.createCustomer(customerService, "Test CS Customer", "29001021234567"));

        assertThrows(UnauthorizedException.class, () ->
                bankService.createCustomer(teller, "Test Teller Customer", "29001031234567"));
    }

    @Test
    @Order(4)
    @DisplayName("Should enforce role-based access for transactions")
    void testRoleBasedAccessTransactions() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Transaction Test", "29001041234567");
        SavingsAccount account = new SavingsAccount(generateAccountNumber(1), customer);
        bankService.openAccount(manager, account);

        assertDoesNotThrow(() ->
                bankService.deposit(manager, generateAccountNumber(1), new BigDecimal("1000")));

        assertDoesNotThrow(() ->
                bankService.withdraw(teller, generateAccountNumber(1), new BigDecimal("100")));
    }

    // =========================================================================
    // CUSTOMER MANAGEMENT
    // =========================================================================

    @Test
    @Order(5)
    @DisplayName("Should create customer with valid National ID")
    void testCreateCustomerValidNationalId() {
        assertDoesNotThrow(() -> {
            Customer customer = bankService.createCustomer(manager, "Ahmed Ali", "29001011234567");
            assertNotNull(customer);
            assertEquals("Ahmed Ali",        customer.getName());
            assertEquals("29001011234567",   customer.getNationalId());
            assertNotNull(customer.getSystemId());
        });
    }

    @Test
    @Order(6)
    @DisplayName("Should reject customer with invalid National ID")
    void testCreateCustomerInvalidNationalId() {
        assertThrows(InvalidNationalIdException.class, () ->
                bankService.createCustomer(manager, "Test User", "123"));

        assertThrows(InvalidNationalIdException.class, () ->
                bankService.createCustomer(manager, "Test User", "ABCD1234567890"));

        assertThrows(InvalidNationalIdException.class, () ->
                bankService.createCustomer(manager, "Test User", "99999991234567"));
    }

    @Test
    @Order(7)
    @DisplayName("Should prevent duplicate National ID")
    void testDuplicateNationalId() throws Exception {
        String nationalId = "29001051234567";
        bankService.createCustomer(manager, "First Customer", nationalId);

        assertThrows(DuplicateNationalIdException.class, () ->
                bankService.createCustomer(manager, "Duplicate Customer", nationalId));
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
        assertEquals(created.getName(),     found.getName());
    }

    @Test
    @Order(9)
    @DisplayName("Should return null for non-existent National ID")
    void testFindNonExistentCustomer() {
        // "99999991234567" is an invalid national ID per Egyptian rules,
        // so findCustomerByNationalId will simply return null (no match in DB).
        Customer notFound = bankService.findCustomerByNationalId("99999991234567");
        assertNull(notFound);
    }

    // =========================================================================
    // ACCOUNT MANAGEMENT
    // =========================================================================

    @Test
    @Order(10)
    @DisplayName("Should create savings account")
    void testCreateSavingsAccount() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Savings Test", "29001071234567");
        SavingsAccount account = new SavingsAccount(generateAccountNumber(10), customer);

        assertDoesNotThrow(() -> bankService.openAccount(manager, account));

        assertEquals(AccountType.SAVINGS,       account.getAccountType());
        assertEquals(new BigDecimal("0.00"),     account.getBalance());

        // Verify account is linked to customer in DB
        Account fromDb = bankService.findAccountByNumber(generateAccountNumber(10));
        assertNotNull(fromDb);
        assertEquals(customer.getSystemId(), fromDb.getOwner().getSystemId());
    }

    @Test
    @Order(11)
    @DisplayName("Should create current account with overdraft")
    void testCreateCurrentAccountWithOverdraft() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Current Test", "29001081234567");
        BigDecimal overdraftLimit = new BigDecimal("5000");
        CurrentAccount account = new CurrentAccount(generateAccountNumber(11), customer, overdraftLimit);

        assertDoesNotThrow(() -> bankService.openAccount(manager, account));

        assertEquals(AccountType.CURRENT,       account.getAccountType());
        assertEquals(overdraftLimit,             account.getOverdraftLimit());
        assertEquals(new BigDecimal("0.00"),     account.getBalance());
    }

    @Test
    @Order(12)
    @DisplayName("Should allow multiple accounts per customer")
    void testMultipleAccountsPerCustomer() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Multi Account", "29001091234567");

        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(120), customer));
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(121), customer));
        bankService.openAccount(manager, new CurrentAccount(generateAccountNumber(122), customer, new BigDecimal("3000")));

        // Verify all 3 exist in DB
        assertNotNull(bankService.findAccountByNumber(generateAccountNumber(120)));
        assertNotNull(bankService.findAccountByNumber(generateAccountNumber(121)));
        assertNotNull(bankService.findAccountByNumber(generateAccountNumber(122)));
    }

    @Test
    @Order(13)
    @DisplayName("Should prevent duplicate account numbers")
    void testDuplicateAccountNumber() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Duplicate Account Test", "29001101234567");
        String accountNumber = generateAccountNumber(13);

        bankService.openAccount(manager, new SavingsAccount(accountNumber, customer));

        assertThrows(DuplicateAccountException.class, () ->
                bankService.openAccount(manager, new SavingsAccount(accountNumber, customer)));
    }

    // =========================================================================
    // TRANSACTION OPERATIONS
    // =========================================================================

    @Test
    @Order(14)
    @DisplayName("Should deposit valid amount")
    void testValidDeposit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Deposit Test", "29001111234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(14), customer));

        BigDecimal depositAmount = new BigDecimal("1000.00");
        assertDoesNotThrow(() ->
                bankService.deposit(teller, generateAccountNumber(14), depositAmount));

        // Read balance from DB — not from the in-memory account object
        Account fromDb = bankService.findAccountByNumber(generateAccountNumber(14));
        assertEquals(depositAmount, fromDb.getBalance());
    }

    @Test
    @Order(15)
    @DisplayName("Should reject negative deposit amount")
    void testNegativeDeposit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Negative Deposit", "29001121234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(15), customer));

        assertThrows(InvalidAmountException.class, () ->
                bankService.deposit(teller, generateAccountNumber(15), new BigDecimal("-100")));
    }

    @Test
    @Order(16)
    @DisplayName("Should reject zero deposit amount")
    void testZeroDeposit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Zero Deposit", "29001131234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(16), customer));

        assertThrows(InvalidAmountException.class, () ->
                bankService.deposit(teller, generateAccountNumber(16), BigDecimal.ZERO));
    }

    @Test
    @Order(17)
    @DisplayName("Should withdraw valid amount from savings account with fee")
    void testValidWithdrawSavings() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Withdraw Test", "29001141234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(17), customer));
        bankService.deposit(teller, generateAccountNumber(17), new BigDecimal("1000"));

        assertDoesNotThrow(() ->
                bankService.withdraw(teller, generateAccountNumber(17), new BigDecimal("500")));

        // 1000 - (500 + 5 fee) = 495
        Account fromDb = bankService.findAccountByNumber(generateAccountNumber(17));
        assertEquals(new BigDecimal("495.00"), fromDb.getBalance());
    }

    @Test
    @Order(18)
    @DisplayName("Should reject withdrawal exceeding balance in savings account")
    void testOverdraftRejectionSavings() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Overdraft Test", "29001151234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(18), customer));
        bankService.deposit(teller, generateAccountNumber(18), new BigDecimal("100"));

        assertThrows(InsufficientAmountException.class, () ->
                bankService.withdraw(teller, generateAccountNumber(18), new BigDecimal("200")));
    }

    @Test
    @Order(19)
    @DisplayName("Should allow withdrawal within overdraft limit for current account with fee")
    void testOverdraftAllowedCurrent() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Current Overdraft", "29001161234567");
        bankService.openAccount(manager, new CurrentAccount(generateAccountNumber(19), customer, new BigDecimal("1000")));
        bankService.deposit(teller, generateAccountNumber(19), new BigDecimal("500"));

        assertDoesNotThrow(() ->
                bankService.withdraw(teller, generateAccountNumber(19), new BigDecimal("1200")));

        // 500 - (1200 + 12 fee) = -712
        Account fromDb = bankService.findAccountByNumber(generateAccountNumber(19));
        assertEquals(new BigDecimal("-712.00"), fromDb.getBalance());
    }

    @Test
    @Order(20)
    @DisplayName("Should reject withdrawal exceeding overdraft limit")
    void testOverdraftExceeded() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Overdraft Exceeded", "29001171234567");
        bankService.openAccount(manager, new CurrentAccount(generateAccountNumber(20), customer, new BigDecimal("500")));
        bankService.deposit(teller, generateAccountNumber(20), new BigDecimal("100"));

        assertThrows(InsufficientAmountException.class, () ->
                bankService.withdraw(teller, generateAccountNumber(20), new BigDecimal("700")));
    }

    // =========================================================================
    // TRANSACTION HISTORY
    // =========================================================================

    @Test
    @Order(21)
    @DisplayName("Should record transaction history correctly")
    void testTransactionHistory() throws Exception {
        Customer customer = bankService.createCustomer(manager, "History Test", "29001181234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(21), customer));

        bankService.deposit(teller,  generateAccountNumber(21), new BigDecimal("1000"));
        bankService.withdraw(teller, generateAccountNumber(21), new BigDecimal("200"));
        bankService.deposit(manager, generateAccountNumber(21), new BigDecimal("500"));

        List<Transaction> transactions = bankService.getTransactionsByAccount(generateAccountNumber(21));

        assertEquals(3, transactions.size());
        assertEquals(TransactionType.DEPOSIT,    transactions.get(0).getType());
        assertEquals(TransactionType.WITHDRAWAL, transactions.get(1).getType());
        assertEquals(TransactionType.DEPOSIT,    transactions.get(2).getType());
    }

    @Test
    @Order(22)
    @DisplayName("Should include employee information in transactions")
    void testTransactionEmployeeAudit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Audit Test", "29001191234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(22), customer));
        bankService.deposit(teller, generateAccountNumber(22), new BigDecimal("1000"));

        List<Transaction> transactions = bankService.getTransactionsByAccount(generateAccountNumber(22));
        Transaction transaction = transactions.get(0);

        assertNotNull(transaction.getPerformedByEmployeeName());
        assertNotNull(transaction.getPerformedByRole());
        assertEquals(teller.getName(),     transaction.getPerformedByEmployeeName());
        assertEquals(EmployeeRole.TELLER,  transaction.getPerformedByRole());
    }

    @Test
    @Order(23)
    @DisplayName("Should record balance after each transaction including fees")
    void testBalanceTracking() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Balance Track", "29001201234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(23), customer));

        bankService.deposit(teller,  generateAccountNumber(23), new BigDecimal("1000"));
        bankService.withdraw(teller, generateAccountNumber(23), new BigDecimal("300"));
        bankService.deposit(teller,  generateAccountNumber(23), new BigDecimal("200"));

        List<Transaction> transactions = bankService.getTransactionsByAccount(generateAccountNumber(23));

        assertEquals(new BigDecimal("1000.00"), transactions.get(0).getBalanceAfter());
        assertEquals(new BigDecimal("697.00"),  transactions.get(1).getBalanceAfter()); // 1000 - 303
        assertEquals(new BigDecimal("897.00"),  transactions.get(2).getBalanceAfter()); // 697 + 200
    }

    // =========================================================================
    // BUSINESS RULES
    // =========================================================================

    @Test
    @Order(24)
    @DisplayName("Should maintain account balance consistency with fees")
    void testBalanceConsistency() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Consistency Test", "29001211234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(24), customer));

        bankService.deposit(teller,  generateAccountNumber(24), new BigDecimal("5000"));
        bankService.withdraw(teller, generateAccountNumber(24), new BigDecimal("1000")); // -1010
        bankService.deposit(teller,  generateAccountNumber(24), new BigDecimal("3000"));
        bankService.withdraw(teller, generateAccountNumber(24), new BigDecimal("2000")); // -2020

        // 5000 - 1010 + 3000 - 2020 = 4970
        Account fromDb = bankService.findAccountByNumber(generateAccountNumber(24));
        assertEquals(new BigDecimal("4970.00"), fromDb.getBalance());
    }

    @Test
    @Order(25)
    @DisplayName("Should generate unique transaction IDs")
    void testUniqueTransactionIds() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Unique ID Test", "29001221234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(25), customer));

        bankService.deposit(teller, generateAccountNumber(25), new BigDecimal("100"));
        bankService.deposit(teller, generateAccountNumber(25), new BigDecimal("200"));
        bankService.deposit(teller, generateAccountNumber(25), new BigDecimal("300"));

        List<Transaction> transactions = bankService.getTransactionsByAccount(generateAccountNumber(25));
        assertEquals(3, transactions.size());

        long uniqueIds = transactions.stream()
                .map(Transaction::getTransactionId)
                .distinct()
                .count();
        assertEquals(3, uniqueIds);
    }

    @Test
    @Order(26)
    @DisplayName("Should handle sequential deposits and withdrawals correctly with fees")
    void testConcurrentOperations() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Concurrent Test", "29001231234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(26), customer));
        bankService.deposit(teller, generateAccountNumber(26), new BigDecimal("10000"));

        for (int i = 0; i < 10; i++) {
            bankService.deposit(teller,  generateAccountNumber(26), new BigDecimal("100"));
            bankService.withdraw(teller, generateAccountNumber(26), new BigDecimal("50"));
        }

        // 10000 + (10×100) - (10×50) - (10×0.50 fee) = 10000 + 1000 - 500 - 5 = 10495
        Account fromDb = bankService.findAccountByNumber(generateAccountNumber(26));
        assertEquals(new BigDecimal("10495.00"), fromDb.getBalance());

        List<Transaction> transactions = bankService.getTransactionsByAccount(generateAccountNumber(26));
        assertEquals(21, transactions.size()); // 1 initial + 10 deposits + 10 withdrawals
    }

    // =========================================================================
    // EDGE CASES
    // =========================================================================

    @Test
    @Order(27)
    @DisplayName("Should handle large deposit amounts")
    void testLargeDeposit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Large Amount", "29001241234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(27), customer));

        BigDecimal largeAmount = new BigDecimal("999999999.99");
        assertDoesNotThrow(() ->
                bankService.deposit(teller, generateAccountNumber(27), largeAmount));

        Account fromDb = bankService.findAccountByNumber(generateAccountNumber(27));
        assertEquals(largeAmount, fromDb.getBalance());
    }

    @Test
    @Order(28)
    @DisplayName("Should handle small decimal amounts")
    void testSmallDecimalAmounts() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Small Decimal", "29001251234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(28), customer));

        assertDoesNotThrow(() ->
                bankService.deposit(teller, generateAccountNumber(28), new BigDecimal("0.01")));

        Account fromDb = bankService.findAccountByNumber(generateAccountNumber(28));
        assertEquals(new BigDecimal("0.01"), fromDb.getBalance());
    }

    @Test
    @Order(29)
    @DisplayName("Should reject deposit to non-existent account number")
    void testInvalidAccountNumber() {
        assertThrows(ResourceNotFoundException.class, () ->
                bankService.deposit(teller, generateAccountNumber(999), new BigDecimal("100")));
    }

    @Test
    @Order(30)
    @DisplayName("Should handle empty transaction history")
    void testEmptyTransactionHistory() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Empty History", "29001261234567");
        bankService.openAccount(manager, new SavingsAccount(generateAccountNumber(30), customer));

        List<Transaction> transactions = bankService.getTransactionsByAccount(generateAccountNumber(30));
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }
}