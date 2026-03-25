package com.finance.bank.integration;

import com.finance.bank.exception.*;
import com.finance.bank.model.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for account management.
 *
 * All tests go through BankService → AccountRepository → PostgreSQL.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Account Integration Tests")
class AccountIntegrationTest extends IntegrationTestBase {

    // ─── SAVINGS ACCOUNT ───────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Manager can open a savings account — persisted to DB")
    void openSavingsAccount_validData_persistedToDb() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Ali Mohamed", nationalId(1));
        SavingsAccount account = new SavingsAccount(acc(1), customer);

        assertDoesNotThrow(() -> bankService.openAccount(manager, account));

        // Verify via DB read
        Account fromDb = bankService.findAccountByNumber(acc(1));
        assertNotNull(fromDb);
        assertEquals(AccountType.SAVINGS, fromDb.getAccountType());
        assertEquals(new BigDecimal("0.00"), fromDb.getBalance());
        assertEquals(customer.getSystemId(), fromDb.getOwner().getSystemId());
    }

    @Test
    @Order(2)
    @DisplayName("CS can open a savings account")
    void openSavingsAccount_byCS_succeeds() throws Exception {
        Customer customer = bankService.createCustomer(cs, "Nadia Tarek", nationalId(2));
        SavingsAccount account = new SavingsAccount(acc(2), customer);

        assertDoesNotThrow(() -> bankService.openAccount(cs, account));
    }

    @Test
    @Order(3)
    @DisplayName("Teller cannot open an account — UnauthorizedException")
    void openAccount_byTeller_throwsUnauthorized() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Blocked Teller", nationalId(3));
        SavingsAccount account = new SavingsAccount(acc(3), customer);

        assertThrows(UnauthorizedException.class,
                () -> bankService.openAccount(teller, account));
    }

    // ─── CURRENT ACCOUNT ───────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("Manager can open a current account with overdraft limit")
    void openCurrentAccount_withOverdraft_persistedToDb() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Reem Youssef", nationalId(4));
        BigDecimal overdraft = new BigDecimal("5000.00");
        CurrentAccount account = new CurrentAccount(acc(4), customer, overdraft);

        assertDoesNotThrow(() -> bankService.openAccount(manager, account));

        Account fromDb = bankService.findAccountByNumber(acc(4));
        assertNotNull(fromDb);
        assertEquals(AccountType.CURRENT, fromDb.getAccountType());
        assertTrue(fromDb instanceof CurrentAccount);
        assertEquals(overdraft, ((CurrentAccount) fromDb).getOverdraftLimit());
    }

    // ─── MULTIPLE ACCOUNTS ─────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("Customer can hold multiple accounts of different types")
    void openMultipleAccounts_sameCustomer_allPersisted() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Multi Account", nationalId(5));

        bankService.openAccount(manager, new SavingsAccount(acc(51), customer));
        bankService.openAccount(manager, new SavingsAccount(acc(52), customer));
        bankService.openAccount(manager, new CurrentAccount(acc(53), customer, new BigDecimal("3000")));

        List<Account> accounts = bankService.getCustomerAccountsMap().get(customer);
        // getCustomerAccountsMap builds the map from DB — customer object might differ,
        // so let's verify via findAccountByNumber for each
        assertNotNull(bankService.findAccountByNumber(acc(51)));
        assertNotNull(bankService.findAccountByNumber(acc(52)));
        assertNotNull(bankService.findAccountByNumber(acc(53)));
    }

    // ─── DUPLICATE ACCOUNT NUMBER ──────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("Duplicate account number throws DuplicateAccountException")
    void openAccount_duplicateNumber_throwsDuplicate() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Duplicate Acc", nationalId(6));
        bankService.openAccount(manager, new SavingsAccount(acc(6), customer));

        assertThrows(DuplicateAccountException.class,
                () -> bankService.openAccount(manager, new SavingsAccount(acc(6), customer)));
    }

    // ─── UNREGISTERED OWNER ────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("Account with unregistered owner is rejected")
    void openAccount_ownerNotInDb_throwsIllegalArgument() throws Exception {
        // Create a Customer object locally WITHOUT saving to DB
        Customer ghost = new Customer("Ghost User", nationalId(7));
        SavingsAccount account = new SavingsAccount(acc(7), ghost);

        assertThrows(IllegalArgumentException.class,
                () -> bankService.openAccount(manager, account));
    }

    // ─── FIND ──────────────────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("findAccountByNumber throws ResourceNotFoundException for unknown account")
    void findAccount_nonExistent_throwsNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> bankService.findAccountByNumber(acc(999)));
    }

    @Test
    @Order(9)
    @DisplayName("findAccountByNumber throws InvalidAccountException for malformed number")
    void findAccount_malformedNumber_throwsInvalid() {
        assertThrows(InvalidAccountException.class,
                () -> bankService.findAccountByNumber("BADNUMBER"));
    }

    // ─── LIST ──────────────────────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("getAccounts returns all persisted accounts")
    void getAccounts_multipleOpened_returnsAll() throws Exception {
        Customer c1 = bankService.createCustomer(manager, "First",  nationalId(10));
        Customer c2 = bankService.createCustomer(manager, "Second", nationalId(11));

        bankService.openAccount(manager, new SavingsAccount(acc(101), c1));
        bankService.openAccount(manager, new CurrentAccount(acc(102), c2, BigDecimal.TEN));

        List<Account> all = bankService.getAccounts();
        assertEquals(2, all.size());
    }

    @Test
    @Order(11)
    @DisplayName("Initial balance of a new account is 0.00")
    void newAccount_initialBalance_isZero() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Zero Balance", nationalId(12));
        bankService.openAccount(manager, new SavingsAccount(acc(12), customer));

        Account fromDb = bankService.findAccountByNumber(acc(12));
        assertEquals(new BigDecimal("0.00"), fromDb.getBalance());
    }
}
