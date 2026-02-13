package com.finance.bank.repository;

import com.finance.bank.model.Transaction;

import java.util.*;
import java.util.stream.Collectors;

public class TransactionRepository {
    // Main storage - all transactions in insertion order
    private final List<Transaction> transactions = new ArrayList<>();

    // Index by account number for faster lookups (preserves insertion order)
    private final Map<String, List<Transaction>> transactionsByAccount = new LinkedHashMap<>();

    /**
     * Saves a transaction record
     * @param transaction Transaction to save
     */
    public void save(Transaction transaction) {
        transactions.add(transaction);

        // Also index by account number for efficient lookup
        transactionsByAccount
                .computeIfAbsent(transaction.getAccountNumber(), k -> new ArrayList<>())
                .add(transaction);
    }

    /**
     * Returns all transactions (read-only)
     * @return Unmodifiable list of all transactions
     */
    public List<Transaction> findAll() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Finds transactions by account number in INSERTION ORDER
     * This is critical for test compatibility - tests expect transactions
     * in the order they were created
     *
     * @param accountNumber Account number to search for
     * @return List of transactions for the account (defensive copy)
     */
    public List<Transaction> findByAccountNumber(String accountNumber) {
        List<Transaction> accountTransactions = transactionsByAccount.get(accountNumber);

        if (accountTransactions == null) {
            return Collections.emptyList();
        }

        // Return defensive copy to prevent external modification
        return new ArrayList<>(accountTransactions);
    }

    /**
     * Finds transactions performed by a specific employee
     * @param employeeId Employee ID
     * @return List of transactions performed by the employee
     */
    public List<Transaction> findByEmployeeId(String employeeId) {
        return transactions.stream()
                .filter(tx -> tx.getPerformedByEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    /**
     * Counts total number of transactions
     * @return Total transaction count
     */
    public int count() {
        return transactions.size();
    }

    /**
     * Counts transactions for a specific account
     * @param accountNumber Account number
     * @return Transaction count for the account
     */
    public int countByAccount(String accountNumber) {
        List<Transaction> accountTransactions = transactionsByAccount.get(accountNumber);
        return accountTransactions != null ? accountTransactions.size() : 0;
    }

    /**
     * CRITICAL FOR TESTING: Clears all transactions
     * Use in BankService.reset() for test isolation
     */
    public void clear() {
        transactions.clear();
        transactionsByAccount.clear();
    }
}