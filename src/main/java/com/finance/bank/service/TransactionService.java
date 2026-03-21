package com.finance.bank.service;

import com.finance.bank.exception.*;
import com.finance.bank.model.*;
import com.finance.bank.repository.AccountRepository;
import com.finance.bank.repository.TransactionRepository;

import java.math.BigDecimal;

public class TransactionService {

    private final AuthorizationService authorizationService;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(AuthorizationService authorizationService,
                              AccountService accountService,
                              TransactionRepository transactionRepository,
                              AccountRepository accountRepository) {
        this.authorizationService = authorizationService;
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }
    public Transaction deposit(Employee employee,
                               String accountNumber,
                               BigDecimal amount) throws InvalidAmountException {

        // 1) Authorization
        authorizationService.ensureCanDeposit(employee);

        // 2) Input validation
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive");
        }

        // 3) Get account (guaranteed non-null)
        Account account = accountService.getAccount(accountNumber);

        // 4) Business action (state mutation only)
        account.deposit(amount);

        // 5) Audit record
        Transaction tx = new Transaction(
                TransactionType.DEPOSIT,
                amount,
                BigDecimal.ZERO,
                account.getBalance(),
                account,
                employee
        );

        // 6) Persist
        transactionRepository.save(tx);
        accountRepository.updateBalance(account.getAccountNumber(), account.getBalance());
        return tx;
    }


    public Transaction withdraw(Employee employee,
                                String accountNumber, BigDecimal amount)
                throws InvalidAmountException, InsufficientAmountException {

        // 1) Authorization
        authorizationService.ensureCanWithdraw(employee);

        // 2) Input validation
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdraw amount must be positive");
        }

        // 3) Get account (throws ResourceNotFoundException if not found)
        Account account = accountService.getAccount(accountNumber);

        // 4) Business action (polymorphism applies: Savings / Current rules)
        account.withdraw(amount);

        // 5) Audit record
        Transaction tx = new Transaction(
                TransactionType.WITHDRAWAL,
                amount,
                BigDecimal.ZERO,
                account.getBalance(),
                account,
                employee
        );


        // 6) Persist transaction
        transactionRepository.save(tx);
        accountRepository.updateBalance(account.getAccountNumber(), account.getBalance());
        return tx;
    }

}
