package com.finance.bank.integration;

import com.finance.bank.exception.*;
import com.finance.bank.model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for deposit, withdrawal, and transaction history.
 *
 * Fee model: 1% of withdrawn amount is deducted on top of the withdrawal.
 *   e.g. withdraw 500 → total deducted = 505, fee = 5
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Transaction Integration Tests")
class TransactionIntegrationTest extends IntegrationTestBase {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.01");

    // ─── Helpers ───────────────────────────────────────────────────────────────

    /** Creates a customer + savings account and returns the account. */
    private Account openSavingsAccount(int index) throws Exception {
        Customer customer = bankService.createCustomer(manager, "Customer " + index, nationalId(index));
        SavingsAccount account = new SavingsAccount(acc(index), customer);
        bankService.openAccount(manager, account);
        return bankService.findAccountByNumber(acc(index)); // fresh from DB
    }

    /** Creates a customer + current account with the given overdraft and returns the account. */
    private Account openCurrentAccount(int index, BigDecimal overdraft) throws Exception {
        Customer customer = bankService.createCustomer(manager, "Current " + index, nationalId(index));
        CurrentAccount account = new CurrentAccount(acc(index), customer, overdraft);
        bankService.openAccount(manager, account);
        return bankService.findAccountByNumber(acc(index));
    }

    // ─── DEPOSIT ───────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Teller deposits valid amount — balance updated in DB")
    void deposit_validAmount_balanceUpdatedInDb() throws Exception {
        Account account = openSavingsAccount(1);

        Transaction tx = bankService.deposit(teller, acc(1), new BigDecimal("1000.00"));

        assertNotNull(tx);
        assertEquals(TransactionType.DEPOSIT, tx.getType());
        assertEquals(new BigDecimal("1000.00"), tx.getAmount());
        assertEquals(new BigDecimal("1000.00"), tx.getBalanceAfter());

        // Verify balance persisted in DB
        Account fromDb = bankService.findAccountByNumber(acc(1));
        assertEquals(new BigDecimal("1000.00"), fromDb.getBalance());
    }

    @Test
    @Order(2)
    @DisplayName("Manager deposits — succeeds with correct balance")
    void deposit_byManager_succeeds() throws Exception {
        Account account = openSavingsAccount(2);
        bankService.deposit(manager, acc(2), new BigDecimal("500.00"));

        Account fromDb = bankService.findAccountByNumber(acc(2));
        assertEquals(new BigDecimal("500.00"), fromDb.getBalance());
    }

    @Test
    @Order(3)
    @DisplayName("CS deposits — succeeds")
    void deposit_byCS_succeeds() throws Exception {
        openSavingsAccount(3);
        assertDoesNotThrow(() -> bankService.deposit(cs, acc(3), new BigDecimal("200.00")));
    }

    @Test
    @Order(4)
    @DisplayName("Deposit zero amount throws InvalidAmountException")
    void deposit_zeroAmount_throwsInvalid() throws Exception {
        openSavingsAccount(4);
        assertThrows(InvalidAmountException.class,
                () -> bankService.deposit(teller, acc(4), BigDecimal.ZERO));
    }

    @Test
    @Order(5)
    @DisplayName("Deposit negative amount throws InvalidAmountException")
    void deposit_negativeAmount_throwsInvalid() throws Exception {
        openSavingsAccount(5);
        assertThrows(InvalidAmountException.class,
                () -> bankService.deposit(teller, acc(5), new BigDecimal("-100")));
    }

    @Test
    @Order(6)
    @DisplayName("Deposit to non-existent account throws ResourceNotFoundException")
    void deposit_nonExistentAccount_throwsNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> bankService.deposit(teller, acc(999), new BigDecimal("100")));
    }

    @Test
    @Order(7)
    @DisplayName("Multiple sequential deposits accumulate correctly")
    void deposit_multipleDeposits_accumulateInDb() throws Exception {
        openSavingsAccount(7);
        bankService.deposit(teller, acc(7), new BigDecimal("1000.00"));
        bankService.deposit(teller, acc(7), new BigDecimal("500.00"));
        bankService.deposit(manager, acc(7), new BigDecimal("250.00"));

        Account fromDb = bankService.findAccountByNumber(acc(7));
        assertEquals(new BigDecimal("1750.00"), fromDb.getBalance());
    }

    // ─── WITHDRAW (SAVINGS) ────────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("Teller withdraws from savings — balance reduced by amount + 1% fee")
    void withdraw_savings_balanceReducedWithFee() throws Exception {
        openSavingsAccount(10);
        bankService.deposit(teller, acc(10), new BigDecimal("1000.00"));

        bankService.withdraw(teller, acc(10), new BigDecimal("500.00"));

        // 500 + 5 (1% fee) = 505 deducted → remaining 495
        Account fromDb = bankService.findAccountByNumber(acc(10));
        assertEquals(new BigDecimal("495.00"), fromDb.getBalance());
    }

    @Test
    @Order(11)
    @DisplayName("Savings account rejects withdrawal exceeding balance")
    void withdraw_savings_exceedsBalance_throwsInsufficient() throws Exception {
        openSavingsAccount(11);
        bankService.deposit(teller, acc(11), new BigDecimal("100.00"));

        assertThrows(InsufficientAmountException.class,
                () -> bankService.withdraw(teller, acc(11), new BigDecimal("200.00")));

        // Balance must be unchanged
        Account fromDb = bankService.findAccountByNumber(acc(11));
        assertEquals(new BigDecimal("100.00"), fromDb.getBalance());
    }

    @Test
    @Order(12)
    @DisplayName("Savings account rejects zero withdrawal")
    void withdraw_savings_zeroAmount_throwsInvalid() throws Exception {
        openSavingsAccount(12);
        bankService.deposit(teller, acc(12), new BigDecimal("500.00"));

        assertThrows(InvalidAmountException.class,
                () -> bankService.withdraw(teller, acc(12), BigDecimal.ZERO));
    }

    // ─── WITHDRAW (CURRENT / OVERDRAFT) ────────────────────────────────────────

    @Test
    @Order(20)
    @DisplayName("Current account allows withdrawal within overdraft limit")
    void withdraw_current_withinOverdraft_succeeds() throws Exception {
        openCurrentAccount(20, new BigDecimal("1000.00"));
        bankService.deposit(teller, acc(20), new BigDecimal("500.00"));

        // Withdraw 1200 → total = 1212 (1% fee) → balance = 500 - 1212 = -712
        assertDoesNotThrow(() -> bankService.withdraw(teller, acc(20), new BigDecimal("1200.00")));

        Account fromDb = bankService.findAccountByNumber(acc(20));
        assertEquals(new BigDecimal("-712.00"), fromDb.getBalance());
    }

    @Test
    @Order(21)
    @DisplayName("Current account rejects withdrawal exceeding overdraft limit")
    void withdraw_current_exceedsOverdraft_throwsInsufficient() throws Exception {
        openCurrentAccount(21, new BigDecimal("500.00"));
        bankService.deposit(teller, acc(21), new BigDecimal("100.00"));

        assertThrows(InsufficientAmountException.class,
                () -> bankService.withdraw(teller, acc(21), new BigDecimal("700.00")));
    }

    // ─── BALANCE CONSISTENCY ───────────────────────────────────────────────────

    @Test
    @Order(30)
    @DisplayName("Complex series of deposits and withdrawals produces correct final balance")
    void balanceConsistency_complexScenario_correct() throws Exception {
        openSavingsAccount(30);

        bankService.deposit(teller,   acc(30), new BigDecimal("5000.00"));
        bankService.withdraw(teller,  acc(30), new BigDecimal("1000.00")); // -1010
        bankService.deposit(teller,   acc(30), new BigDecimal("3000.00"));
        bankService.withdraw(manager, acc(30), new BigDecimal("2000.00")); // -2020

        // 5000 - 1010 + 3000 - 2020 = 4970
        Account fromDb = bankService.findAccountByNumber(acc(30));
        assertEquals(new BigDecimal("4970.00"), fromDb.getBalance());
    }

    // ─── TRANSACTION HISTORY ───────────────────────────────────────────────────

    @Test
    @Order(40)
    @DisplayName("Transaction history is saved to DB in insertion order")
    void transactionHistory_savedInOrder() throws Exception {
        openSavingsAccount(40);
        bankService.deposit(teller,  acc(40), new BigDecimal("1000.00"));
        bankService.withdraw(teller, acc(40), new BigDecimal("200.00"));
        bankService.deposit(manager, acc(40), new BigDecimal("500.00"));

        List<Transaction> txs = bankService.getTransactionsByAccount(acc(40));

        assertEquals(3, txs.size());
        assertEquals(TransactionType.DEPOSIT,    txs.get(0).getType());
        assertEquals(TransactionType.WITHDRAWAL, txs.get(1).getType());
        assertEquals(TransactionType.DEPOSIT,    txs.get(2).getType());
    }

    @Test
    @Order(41)
    @DisplayName("Transaction records correct balanceAfter including fee")
    void transactionHistory_correctBalanceAfterWithFee() throws Exception {
        openSavingsAccount(41);
        bankService.deposit(teller,  acc(41), new BigDecimal("1000.00"));
        bankService.withdraw(teller, acc(41), new BigDecimal("300.00")); // fee=3, total=303
        bankService.deposit(teller,  acc(41), new BigDecimal("200.00"));

        List<Transaction> txs = bankService.getTransactionsByAccount(acc(41));

        assertEquals(new BigDecimal("1000.00"), txs.get(0).getBalanceAfter());
        assertEquals(new BigDecimal("697.00"),  txs.get(1).getBalanceAfter()); // 1000 - 303
        assertEquals(new BigDecimal("897.00"),  txs.get(2).getBalanceAfter()); // 697 + 200
    }

    @Test
    @Order(42)
    @DisplayName("Transaction records the performing employee's name and role")
    void transactionHistory_auditTrail_employeeInfo() throws Exception {
        openSavingsAccount(42);
        bankService.deposit(teller, acc(42), new BigDecimal("1000.00"));

        List<Transaction> txs = bankService.getTransactionsByAccount(acc(42));
        Transaction tx = txs.get(0);

        assertEquals(teller.getName(),    tx.getPerformedByEmployeeName());
        assertEquals(EmployeeRole.TELLER, tx.getPerformedByRole());
        assertNotNull(tx.getPerformedByEmployeeId());
    }

    @Test
    @Order(43)
    @DisplayName("Transaction IDs are unique across multiple transactions")
    void transactionHistory_uniqueTransactionIds() throws Exception {
        openSavingsAccount(43);
        bankService.deposit(teller, acc(43), new BigDecimal("100.00"));
        bankService.deposit(teller, acc(43), new BigDecimal("200.00"));
        bankService.deposit(teller, acc(43), new BigDecimal("300.00"));

        List<Transaction> txs = bankService.getTransactionsByAccount(acc(43));
        assertEquals(3, txs.size());

        long uniqueIds = txs.stream().map(Transaction::getTransactionId).distinct().count();
        assertEquals(3, uniqueIds, "All transaction IDs must be unique");
    }

    @Test
    @Order(44)
    @DisplayName("getTransactionsByAccount returns empty list for account with no transactions")
    void transactionHistory_emptyAccount_returnsEmptyList() throws Exception {
        openSavingsAccount(44);
        List<Transaction> txs = bankService.getTransactionsByAccount(acc(44));

        assertNotNull(txs);
        assertTrue(txs.isEmpty());
    }

    @Test
    @Order(45)
    @DisplayName("Transactions are isolated per account")
    void transactionHistory_isolatedPerAccount() throws Exception {
        openSavingsAccount(45);
        openSavingsAccount(46);

        bankService.deposit(teller, acc(45), new BigDecimal("1000.00"));
        bankService.deposit(teller, acc(45), new BigDecimal("500.00"));
        bankService.deposit(teller, acc(46), new BigDecimal("200.00"));

        List<Transaction> txs45 = bankService.getTransactionsByAccount(acc(45));
        List<Transaction> txs46 = bankService.getTransactionsByAccount(acc(46));

        assertEquals(2, txs45.size(), "Account 45 should have 2 transactions");
        assertEquals(1, txs46.size(), "Account 46 should have 1 transaction");
    }

    // ─── LARGE / EDGE AMOUNTS ──────────────────────────────────────────────────

    @Test
    @Order(50)
    @DisplayName("Large deposit amount is persisted correctly")
    void deposit_largeAmount_persistedCorrectly() throws Exception {
        openSavingsAccount(50);
        BigDecimal large = new BigDecimal("999999999.99");

        bankService.deposit(teller, acc(50), large);

        Account fromDb = bankService.findAccountByNumber(acc(50));
        assertEquals(large, fromDb.getBalance());
    }

    @Test
    @Order(51)
    @DisplayName("Minimum possible deposit (0.01) is persisted correctly")
    void deposit_minimalAmount_persistedCorrectly() throws Exception {
        openSavingsAccount(51);
        bankService.deposit(teller, acc(51), new BigDecimal("0.01"));

        Account fromDb = bankService.findAccountByNumber(acc(51));
        assertEquals(new BigDecimal("0.01"), fromDb.getBalance());
    }
}
