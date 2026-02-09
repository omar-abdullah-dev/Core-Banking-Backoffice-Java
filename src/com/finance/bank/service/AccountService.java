package com.finance.bank.service;

import com.finance.bank.exception.ResourceNotFoundException;
import com.finance.bank.model.*;
import com.finance.bank.repository.AccountRepository;

import java.math.BigDecimal;

public class AccountService {

    private final AuthorizationService authorizationService;
    private final AccountRepository accountRepository;

    public AccountService(AuthorizationService authorizationService,
                          AccountRepository accountRepository) {
        this.authorizationService = authorizationService;
        this.accountRepository = accountRepository;
    }

    /** Create a new account and attach it to a customer */
    public Account createAccount(Employee employee,AccountType type,String accountNumber,
                                 Customer owner, BigDecimal overdraftLimit)
    {

        authorizationService.ensureCanAddAccount(employee);

        Account account = switch (type) {
            case SAVINGS -> new SavingsAccount(accountNumber, owner);
            case CURRENT -> new CurrentAccount(accountNumber, owner, overdraftLimit);
        };

        accountRepository.save(account);
        return account;
    }


    /** Retrieve an existing account (guaranteed non-null) */
    public Account getAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }

        Account account = accountRepository.findByNumber(accountNumber);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }
        return account;
    }

}
