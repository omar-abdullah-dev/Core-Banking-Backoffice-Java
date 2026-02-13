package com.finance.bank.service;

import com.finance.bank.exception.*;
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

    /**
     * Creates a new account and attaches it to a customer
     * @param employee Employee creating the account
     * @param type Account type (SAVINGS or CURRENT)
     * @param accountNumber Account number
     * @param owner Customer who owns the account
     * @param overdraftLimit Overdraft limit (only for CURRENT accounts)
     * @return Created account
     * @throws UnauthorizedException if employee lacks permission
     * @throws DuplicateAccountException if account number already exists
     * @throws InvalidAccountException if account data is invalid
     */
    public Account createAccount(Employee employee,
                                 AccountType type,
                                 String accountNumber,
                                 Customer owner,
                                 BigDecimal overdraftLimit)
            throws UnauthorizedException, DuplicateAccountException, InvalidAccountException {

        // Authorization check
        authorizationService.ensureCanAddAccount(employee);

        // Create account based on type
        Account account = switch (type) {
            case SAVINGS -> new SavingsAccount(accountNumber, owner);
            case CURRENT -> new CurrentAccount(accountNumber, owner, overdraftLimit);
        };

        // Save to repository (may throw DuplicateAccountException)
        accountRepository.save(account);

        return account;
    }

    /**
     * Gets an account by account number
     * @param accountNumber Account number to search for
     * @return Account if found
     * @throws ResourceNotFoundException if account not found
     */
    public Account getAccount(String accountNumber) throws ResourceNotFoundException {
        Account account = accountRepository.findByNumber(accountNumber);

        if (account == null) {
            throw new ResourceNotFoundException(
                    "Account with number " + accountNumber + " not found"
            );
        }

        return account;
    }

    /**
     * Checks if an account exists
     * @param accountNumber Account number to check
     * @return true if exists, false otherwise
     */
    public boolean accountExists(String accountNumber) {
        return accountRepository.exists(accountNumber);
    }
}