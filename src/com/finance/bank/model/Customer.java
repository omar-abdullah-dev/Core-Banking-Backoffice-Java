package com.finance.bank.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer extends Person {

    private final List<Account> accounts;

    public Customer(String name, String nationalId) {
        super(name, nationalId);
        this.accounts = new ArrayList<>();
    }

    public Customer(String systemId, String name, String nationalId) {
        super(systemId, name, nationalId);
        this.accounts = new ArrayList<>();
    }

    public Customer(String systemId, String name, String nationalId, String emailAddress, String phoneNumber) {
        super(systemId, name, nationalId, emailAddress, phoneNumber);
        this.accounts = new ArrayList<>();
    }

    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public void addAccount(Account account) {
        if (account != null)
            accounts.add(account);
    }
}
