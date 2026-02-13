package com.finance.bank.repository;

import com.finance.bank.exception.DuplicateAccountException;
import com.finance.bank.exception.ResourceNotFoundException;
import com.finance.bank.model.Account;

import java.util.*;

public class AccountRepository {
    private final Map<String, Account> accounts = new HashMap<>();

    /**
     * Saves an account to the repository
     * @param account Account to save
     * @throws DuplicateAccountException if account number already exists
     */
    public void save(Account account) throws DuplicateAccountException {
        if (accounts.containsKey(account.getAccountNumber())) {
            throw new DuplicateAccountException(
                    "Account with number " + account.getAccountNumber() + " already exists"
            );
        }
        accounts.put(account.getAccountNumber(), account);
    }

    /**
     * Finds account by account number
     * @param accountNumber Account number to search for
     * @return Account if found, null otherwise
     */
    public Account findByNumber(String accountNumber) {
        return accounts.get(accountNumber);
    }

    /**
     * Finds account by account number, throws exception if not found
     * This is used by BankService operations that require the account to exist
     *
     * @param accountNumber Account number to search for
     * @return Account if found
     * @throws ResourceNotFoundException if account not found
     */
    public Account findByNumberOrThrow(String accountNumber) throws ResourceNotFoundException {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new ResourceNotFoundException(
                    "Account with number " + accountNumber + " not found"
            );
        }
        return account;
    }

    /**
     * Checks if an account exists with the given account number
     * @param accountNumber Account number to check
     * @return true if exists, false otherwise
     */
    public boolean exists(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    /**
     * Returns all accounts as a list
     * @return List of all accounts
     */
    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    /**
     * CRITICAL FOR TESTING: Clears all accounts
     * Use in BankService.reset() for test isolation
     */
    public void clear() {
        accounts.clear();
    }
}