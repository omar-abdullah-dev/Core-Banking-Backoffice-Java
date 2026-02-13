package com.finance.bank.service;

import com.finance.bank.exception.*;
import com.finance.bank.model.Account;
import com.finance.bank.model.Customer;
import com.finance.bank.model.Employee;
import com.finance.bank.model.Transaction;
import com.finance.bank.repository.AccountRepository;
import com.finance.bank.repository.CustomerRepository;
import com.finance.bank.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.*;

import static com.finance.bank.util.AccountValidator.validateAccountNumber;

public class BankService {

    // CRITICAL CHANGE: Use CustomerRepository instead of direct Map
    private final CustomerRepository customerRepository;

    // Other repositories
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Services
    private final AuthorizationService authorizationService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    // Singleton holder pattern
    private static class BankServiceHolder {
        private static final BankService INSTANCE = new BankService();
    }

    private BankService() {
        // CRITICAL: Initialize CustomerRepository
        this.customerRepository = new CustomerRepository();

        // Initialize other repositories
        this.accountRepository = new AccountRepository();
        this.transactionRepository = new TransactionRepository();

        // Initialize services
        this.authorizationService = new AuthorizationService();
        this.accountService = new AccountService(authorizationService, accountRepository);
        this.transactionService = new TransactionService(
                authorizationService,
                accountService,
                transactionRepository
        );
    }

    public static BankService getInstance() {
        return BankServiceHolder.INSTANCE;
    }

    /**
     * CRITICAL FOR TESTING: Resets all repositories to clean state
     *
     * Call this in @BeforeEach to ensure test isolation:
     *
     * @BeforeEach
     * void setUp() {
     *     BankService.getInstance().reset();
     * }
     */
    public void reset() {
        customerRepository.clear();
        accountRepository.clear();
        transactionRepository.clear();
    }

    /**
     * Creates a new customer
     * @param employee Employee creating the customer (must have CS or MANAGER role)
     * @param name Customer name
     * @param nationalId Egyptian National ID
     * @return Created customer
     * @throws UnauthorizedException if employee lacks permission
     * @throws DuplicateNationalIdException if National ID already exists
     * @throws InvalidNationalIdException if National ID is invalid
     */
    public Customer createCustomer(Employee employee, String name, String nationalId)
            throws UnauthorizedException, DuplicateNationalIdException, InvalidNationalIdException {

        // Authorization check
        authorizationService.ensureCanCreateCustomer(employee);

        // CRITICAL: Use repository to create customer (includes validation)
        return customerRepository.save(name, nationalId);
    }

    /**
     * Opens a new account for a customer
     * @param employee Employee creating the account (must have CS or MANAGER role)
     * @param account Account to open
     * @throws UnauthorizedException if employee lacks permission
     * @throws DuplicateAccountException if account number already exists
     * @throws InvalidAccountException if account is invalid
     */
    public void openAccount(Employee employee, Account account)
            throws UnauthorizedException, DuplicateAccountException, InvalidAccountException {

        // Authorization: only CS or MANAGER can add accounts
        authorizationService.ensureCanAddAccount(employee);

        if (account == null) {
            throw new InvalidAccountException("Account cannot be null");
        }

        String accNum = account.getAccountNumber();
        if (accNum == null || accNum.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }

        // Validate structure (length, prefix, digits, etc.)
        validateAccountNumber(accNum);

        // Check duplicate using repository
        if (accountRepository.exists(accNum)) {
            throw new DuplicateAccountException(
                    "Account with this account number already exists"
            );
        }

        Customer owner = account.getOwner();
        if (owner == null) {
            throw new IllegalArgumentException("Account owner cannot be null");
        }

        // Ensure owner is registered as a customer
        Customer existingCustomer = customerRepository.findByNationalId(owner.getNationalId());
        if (existingCustomer == null) {
            throw new IllegalArgumentException(
                    "Account owner is not registered as a customer"
            );
        }

        // Persist account and attach to customer
        // Note: save() may throw DuplicateAccountException but we already checked above
        accountRepository.save(account);
        owner.addAccount(account);
    }

    /**
     * Deposits money into an account
     * @param employee Employee performing the deposit (must have TELLER or MANAGER role)
     * @param accountNumber Account number
     * @param amount Amount to deposit
     * @return Transaction record
     * @throws UnauthorizedException if employee lacks permission
     * @throws InvalidAmountException if amount is invalid
     * @throws ResourceNotFoundException if account not found
     */
    public Transaction deposit(Employee employee, String accountNumber, BigDecimal amount)
            throws UnauthorizedException, InvalidAmountException, ResourceNotFoundException {
        return transactionService.deposit(employee, accountNumber, amount);
    }

    /**
     * Withdraws money from an account
     * @param employee Employee performing the withdrawal (must have TELLER or MANAGER role)
     * @param accountNumber Account number
     * @param amount Amount to withdraw
     * @return Transaction record
     * @throws UnauthorizedException if employee lacks permission
     * @throws InvalidAmountException if amount is invalid
     * @throws InsufficientAmountException if insufficient balance/overdraft
     * @throws ResourceNotFoundException if account not found
     */
    public Transaction withdraw(Employee employee, String accountNumber, BigDecimal amount)
            throws UnauthorizedException, InvalidAmountException, InsufficientAmountException, ResourceNotFoundException {
        return transactionService.withdraw(employee, accountNumber, amount);
    }

    /**
     * Gets transaction history for an account
     * @param accountNumber Account number
     * @return List of transactions in insertion order
     */
    public List<Transaction> getTransactionsByAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }

        return transactionRepository.findByAccountNumber(accountNumber);
    }

    /**
     * Finds customer by National ID
     * @param nationalId Egyptian National ID
     * @return Customer if found, null otherwise
     */
    public Customer findCustomerByNationalId(String nationalId) {
        return customerRepository.findByNationalId(nationalId);
    }

    /**
     * Finds account by account number
     * @param accountNumber Account number
     * @return Account if found
     * @throws InvalidAccountException if account number is invalid
     * @throws ResourceNotFoundException if account not found
     */
    public Account findAccountByNumber(String accountNumber)
            throws InvalidAccountException, ResourceNotFoundException {

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new InvalidAccountException("Account number cannot be null or empty");
        }

        validateAccountNumber(accountNumber);

        Account account = accountRepository.findByNumber(accountNumber);

        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }

        return account;
    }

    /**
     * Transfers funds between accounts
     * @param fromAccount Source account number
     * @param toAccount Destination account number
     * @param amount Amount to transfer
     * @throws InvalidAccountException if account is invalid
     * @throws ResourceNotFoundException if account not found
     */
    public void transferFunds(String fromAccount, String toAccount, BigDecimal amount)
            throws InvalidAccountException, ResourceNotFoundException {

        if (fromAccount == null) {
            throw new ResourceNotFoundException("From account cannot be null");
        }
        if (toAccount == null) {
            throw new ResourceNotFoundException("To account cannot be null");
        }

        Account from = findAccountByNumber(fromAccount);
        Account to = findAccountByNumber(toAccount);

        try {
            from.withdraw(amount);
        } catch (InvalidAmountException | InsufficientAmountException e) {
            throw new RuntimeException(e);
        }

        try {
            to.deposit(amount);
        } catch (InvalidAmountException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns all customers (defensive copy)
     * @return Unmodifiable list of customers
     */
    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customerRepository.findAll());
    }

    /**
     * Returns all accounts (defensive copy)
     * @return Unmodifiable list of accounts
     */
    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accountRepository.findAll());
    }

    /**
     * Returns customer-to-accounts mapping
     * @return Unmodifiable map of customers to their accounts
     */
    public Map<Customer, List<Account>> getCustomerAccountsMap() {
        Map<Customer, List<Account>> customerAccountsMap = new HashMap<>();
        for (Customer customer : customerRepository.findAll()) {
            customerAccountsMap.put(customer, customer.getAccounts());
        }
        return Collections.unmodifiableMap(customerAccountsMap);
    }
}