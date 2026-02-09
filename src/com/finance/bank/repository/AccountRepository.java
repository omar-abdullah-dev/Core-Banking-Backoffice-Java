package com.finance.bank.repository;

import com.finance.bank.model.Account;

import java.util.HashMap;
import java.util.Map;

public class AccountRepository {

    private final Map<String, Account> accounts = new HashMap<>();

    public void save(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }

    public Account findByNumber(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public boolean exists(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }
}
