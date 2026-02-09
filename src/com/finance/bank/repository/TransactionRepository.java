package com.finance.bank.repository;

import com.finance.bank.model.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionRepository {

    private final List<Transaction> transactions = new ArrayList<>();

    /** Save a transaction record */
    public void save(Transaction transaction) {
        transactions.add(transaction);
    }

    /** Return all transactions (read-only) */
    public List<Transaction> findAll() {
        return Collections.unmodifiableList(transactions);
    }

    /** Find transactions by account number */
    public List<Transaction> findByAccountNumber(String accountNumber) {
        return transactions.stream()
                .filter(tx -> tx.getAccountNumber().equals(accountNumber))
                .collect(Collectors.toList());
    }

    /** Find transactions performed by a specific employee */
    public List<Transaction> findByEmployeeId(String employeeId) {
        return transactions.stream()
                .filter(tx -> tx.getPerformedByEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }
}
