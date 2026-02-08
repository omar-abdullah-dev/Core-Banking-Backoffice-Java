package com.finance.bank.model;

import com.finance.bank.exception.InsufficientAmountException;
import com.finance.bank.exception.InvalidAccountException;
import com.finance.bank.exception.InvalidAmountException;
import com.finance.bank.util.AccountValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


abstract public class Account {
    private static final int ACCOUNT_NUMBER_LENGTH = 16;
    private static final BigDecimal WITHDRAW_FEE_PERCENT = BigDecimal.valueOf(0.01); // 0.01%;

    private final Customer owner;
    private final AccountType accountType;
    protected BigDecimal balance;
    private final List<Transaction> transactions = new ArrayList<>();
    //    validate: only numbers + length 16 + not changeable (Bank code + Branch code + Serial)
    private final String accountNumber;



//    Composition Relationship & validate accountNumber
protected Account(String accountNumber, Customer owner, AccountType accountType) throws InvalidAccountException {
    this.accountType = accountType;

    // validate (throw on error)
    AccountValidator.validateAccountNumber(accountNumber);
    AccountValidator.validateOwner(owner);

    this.accountNumber = accountNumber;
    this.owner = owner;
    this.balance = BigDecimal.ZERO;
}

    public String getAccountNumber() {
        return accountNumber;
    }
    public AccountType getAccountType() { return accountType; }

    public BigDecimal getBalance() {
        return balance;
    }

    public Customer getOwner() {
        return owner;
    }

    public void deposit(BigDecimal amount) throws InvalidAmountException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new InvalidAmountException("Invalid amount");
        }
        balance = balance.add(amount);

        Transaction transaction = new Transaction(TransactionType.DEPOSIT, amount,new BigDecimal("0.0"), this.balance);
//        record transaction✅
        recordTransaction(transaction);
    }

    public abstract void withdraw(BigDecimal amount) throws InvalidAmountException, InsufficientAmountException;
    // helper for printing
    public String getTypeName() { return accountType.label(); }



    public static int getAccountNumberLength() {
        return ACCOUNT_NUMBER_LENGTH;
    }

    public static BigDecimal getWithdrawFeePercent() {
        return WITHDRAW_FEE_PERCENT;
    }
//  protected method to be used by child classes to record transactions only
    protected void recordTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }
//    to return unmodifiable list of transactions cannot be changed from outside child classes
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
// TODO: override getTransactions in child classes to add filtering by date/type etc.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountNumber != null && accountNumber.equals(account.accountNumber);
    }

    @Override
    public int hashCode() {
        return accountNumber != null ? accountNumber.hashCode() : 0;
    }


    //    polymorphism
    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", ownerNationalId=" + (owner != null ? owner.getNationalId() : "null") +
                '}';
    }

}
