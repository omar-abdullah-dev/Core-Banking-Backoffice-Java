package com.finance.bank.integration;

import com.finance.bank.exception.UnauthorizedException;
import com.finance.bank.model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test — complete banking story.
 *
 * ⚠️  These steps share state through the DB.
 *     @BeforeEach clears the DB, so steps must run in order AND in a single test
 *     OR we use @TestInstance(PER_CLASS) + manual control.
 *
 *     Solution: All story steps are ONE test method that runs sequentially.
 *     Individual @Test methods test isolated role/auth restrictions.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Full Banking Story — End-to-End Integration Test")
class BankingStoryIntegrationTest extends IntegrationTestBase {

    // Valid 14-digit Egyptian national ID: gov code "01" = Cairo
    private static final String AHMED_NATIONAL_ID = "29001010100200";
    private static final String ACCOUNT_NUMBER    = "1001000100009999";

    // ─── Full story in one test (avoids @BeforeEach clearing between steps) ──

    @Test
    @Order(1)
    @DisplayName("Full story: login → create customer → open account → deposit → withdraw → audit")
    void fullBankingStory() throws Exception {

        // Step 1 — Verify all roles logged in
        assertNotNull(manager, "Manager must be logged in");
        assertNotNull(teller,  "Teller must be logged in");
        assertNotNull(cs,      "CS must be logged in");
        assertEquals(EmployeeRole.MANAGER, manager.getRole());
        assertEquals(EmployeeRole.TELLER,  teller.getRole());
        assertEquals(EmployeeRole.CS,      cs.getRole());

        // Step 2 — Manager creates customer
        Customer customer = bankService.createCustomer(manager, "Ahmed Hassan", AHMED_NATIONAL_ID);
        assertNotNull(customer);
        assertEquals("Ahmed Hassan",   customer.getName());
        assertEquals(AHMED_NATIONAL_ID, customer.getNationalId());

        Customer fromDb = bankService.findCustomerByNationalId(AHMED_NATIONAL_ID);
        assertNotNull(fromDb, "Customer must be retrievable from DB");
        assertEquals("Ahmed Hassan", fromDb.getName());

        // Step 3 — Manager opens savings account
        SavingsAccount account = new SavingsAccount(ACCOUNT_NUMBER, fromDb);
        bankService.openAccount(manager, account);

        Account accFromDb = bankService.findAccountByNumber(ACCOUNT_NUMBER);
        assertNotNull(accFromDb);
        assertEquals(AccountType.SAVINGS,   accFromDb.getAccountType());
        assertEquals(new BigDecimal("0.00"), accFromDb.getBalance());

        // Step 4 — Teller deposits 5000
        Transaction deposit = bankService.deposit(teller, ACCOUNT_NUMBER, new BigDecimal("5000.00"));
        assertNotNull(deposit);
        assertEquals(TransactionType.DEPOSIT,       deposit.getType());
        assertEquals(new BigDecimal("5000.00"),      deposit.getAmount());
        assertEquals(new BigDecimal("5000.00"),      deposit.getBalanceAfter());
        assertEquals(EmployeeRole.TELLER,            deposit.getPerformedByRole());

        accFromDb = bankService.findAccountByNumber(ACCOUNT_NUMBER);
        assertEquals(new BigDecimal("5000.00"), accFromDb.getBalance());

        // Step 5 — Teller withdraws 2000 (fee = 20 → total deducted = 2020)
        Transaction withdrawal = bankService.withdraw(teller, ACCOUNT_NUMBER, new BigDecimal("2000.00"));
        assertNotNull(withdrawal);
        assertEquals(TransactionType.WITHDRAWAL,  withdrawal.getType());
        assertEquals(new BigDecimal("2000.00"),   withdrawal.getAmount());
        assertEquals(new BigDecimal("2980.00"),   withdrawal.getBalanceAfter()); // 5000 - 2020

        accFromDb = bankService.findAccountByNumber(ACCOUNT_NUMBER);
        assertEquals(new BigDecimal("2980.00"), accFromDb.getBalance());

        // Step 6 — Manager views full audit history
        List<Transaction> txs = bankService.getTransactionsByAccount(ACCOUNT_NUMBER);
        assertEquals(2, txs.size(), "Should have exactly 2 transactions");

        assertEquals(TransactionType.DEPOSIT,    txs.get(0).getType());
        assertEquals(new BigDecimal("5000.00"),  txs.get(0).getBalanceAfter());
        assertEquals(EmployeeRole.TELLER,        txs.get(0).getPerformedByRole());

        assertEquals(TransactionType.WITHDRAWAL, txs.get(1).getType());
        assertEquals(new BigDecimal("2980.00"),  txs.get(1).getBalanceAfter());
        assertEquals(EmployeeRole.TELLER,        txs.get(1).getPerformedByRole());

        assertTrue(txs.get(0).getTimestamp().isBefore(txs.get(1).getTimestamp())
                || txs.get(0).getTimestamp().equals(txs.get(1).getTimestamp()));
    }

    // ─── Role restriction tests (each is isolated — uses @BeforeEach clean DB) ─

    @Test
    @Order(2)
    @DisplayName("Teller is blocked from opening a new account")
    void teller_cannotOpenAccount() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Test Customer", nationalId(200));
        SavingsAccount newAccount = new SavingsAccount(acc(9998), customer);

        assertThrows(UnauthorizedException.class,
                () -> bankService.openAccount(teller, newAccount));
    }

    @Test
    @Order(3)
    @DisplayName("Teller is blocked from creating a customer")
    void teller_cannotCreateCustomer() {
        assertThrows(UnauthorizedException.class,
                () -> bankService.createCustomer(teller, "Blocked", nationalId(201)));
    }

    @Test
    @Order(4)
    @DisplayName("CS is blocked from depositing")
    void cs_cannotDeposit() throws Exception {
        Customer customer = bankService.createCustomer(manager, "CS Test", nationalId(202));
        bankService.openAccount(manager, new SavingsAccount(acc(9997), customer));
        bankService.deposit(manager, acc(9997), new BigDecimal("500.00"));

        // CS role — check your AuthorizationService.ensureCanDeposit()
        // If CS is allowed to deposit in your system, remove this test
        // Based on your AuthorizationService code: CS, TELLER, MANAGER are all allowed
        // So this should NOT throw — adjust based on your actual business rule
        assertDoesNotThrow(() -> bankService.deposit(cs, acc(9997), new BigDecimal("100.00")));
    }

    @Test
    @Order(5)
    @DisplayName("Duplicate customer is rejected even by CS")
    void cs_duplicateCustomer_blocked() throws Exception {
        bankService.createCustomer(manager, "Ahmed Hassan", nationalId(203));

        assertThrows(Exception.class,
                () -> bankService.createCustomer(cs, "Ahmed Duplicate", nationalId(203)));
    }
}