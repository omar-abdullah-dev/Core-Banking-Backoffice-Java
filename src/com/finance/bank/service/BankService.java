package com.finance.bank.service;

import com.finance.bank.exception.*;
import com.finance.bank.model.Account;
import com.finance.bank.model.Customer;
import com.finance.bank.util.IdGenerator;
import com.finance.bank.util.NationalIdValidator;

import java.math.BigDecimal;
import java.util.*;

import static com.finance.bank.util.AccountValidator.validateAccountNumber;


public class BankService {
    private final Map<String, Customer> customersByNationalId = new HashMap<>();
//    private final List<Account> accounts = new ArrayList<>();
    private final Map<String, Account> accountsByNumber = new HashMap<>();


    private BankService() {

    }
    private static class BankServiceHolder {
        private static final BankService INSTANCE = new BankService();
    }
    public static BankService getInstance() {
        return BankServiceHolder.INSTANCE;
    }



    //    create customer method
public Customer createCustomer(String name, String nationalId)
        throws DuplicateNationalIdException, InvalidNationalIdException {

    if (nationalId == null) throw new InvalidNationalIdException("National ID cannot be null");

    nationalId = nationalId.trim();
    NationalIdValidator.validateNationalId(nationalId);
    name = (name == null) ? null : name.trim();

    if (customersByNationalId.containsKey(nationalId)) {
        throw new DuplicateNationalIdException("Customer with this nationalId already exists");
    }

    String systemId = IdGenerator.generateCustomerId();
    Customer customer = new Customer(systemId, name, nationalId);
    customersByNationalId.put(nationalId, customer);
    return customer;
}


    //    open account method --> Polymorphism
    public void openAccount(Account account)
            throws DuplicateAccountException, InvalidAccountException {

        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }

        String accNum = account.getAccountNumber();
        if (accNum == null || accNum.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }

        // validate structure (length, prefix, digits, etc.)
        validateAccountNumber(accNum);

        // check duplicate
        /*
        * “I refactored account storage from a List to a Map keyed
        * by account number to ensure constant‑time lookups
        * and enforce uniqueness at the data‑structure level.”
         */
        if (accountsByNumber.containsKey(accNum)) {
            throw new DuplicateAccountException(
                    "Account with this account number already exists"
            );
        }

        Customer owner = account.getOwner();
        if (owner == null) {
            throw new IllegalArgumentException("Account owner cannot be null");
        }

        // ensure owner is registered as a customer
        if (!customersByNationalId.containsKey(owner.getNationalId())) {
            throw new IllegalArgumentException(
                    "Account owner is not registered as a customer"
            );
        }

        // persist
        accountsByNumber.put(accNum, account);
        owner.addAccount(account);
    }

    public Customer findCustomerByNationalId(String nationalId) {
        return nationalId == null ? null : customersByNationalId.get(nationalId.trim());
    }
    public Account findAccountByNumber(String accountNumber) {
        if (accountNumber == null) throw new InvalidAccountException("Account number cannot be null");
        try {
            validateAccountNumber(accountNumber);
        } catch (InvalidAccountException e) {
            throw new RuntimeException(e);
        }
        if (accountsByNumber.containsKey(accountNumber)) {
            return accountsByNumber.get(accountNumber);
        }
        return null;
    }
//  TODO: transfer funds method --> withdraw from one account and deposit to another
    public void transferFunds(String fromAccount, String toAccount, BigDecimal amount)
     throws InvalidAccountException, InvalidNationalIdException {
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

            //    Defensive Copy Pattern
            //    I return unmodifiable views from the service layer
            //    to protect internal state and enforce encapsulation.
    /*****  not logically needed but just to practice on Collections usability****/
        public List<Customer> getCustomers() {
            return Collections.unmodifiableList(
                    new ArrayList<>(customersByNationalId.values())
            );
        }
    //    not needed but just to practice on Collections usability
        public List<Account> getAccounts() {
            return Collections.unmodifiableList(new ArrayList<>(accountsByNumber.values()));
        }
    //    not needed but just to show mapping usability
        public Map<Customer, List<Account>> getCustomerAccountsMap() {
            Map<Customer, List<Account>> customerAccountsMap = new HashMap<>();
            for (Customer customer : customersByNationalId.values()) {
                customerAccountsMap.put(customer, customer.getAccounts());
            }
            return Collections.unmodifiableMap(customerAccountsMap);
        }

}
