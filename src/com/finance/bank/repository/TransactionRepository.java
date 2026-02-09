package com.finance.bank.repository;

import com.finance.bank.model.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionRepository {

    private final List<Transaction> transactions = new ArrayList<>();

    public void save(Transaction transaction) {
        transactions.add(transaction);
    }

    public List<Transaction> findAll() {
        return Collections.unmodifiableList(transactions);
    }
}
