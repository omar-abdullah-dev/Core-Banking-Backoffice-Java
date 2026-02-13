package com.finance.bank.model;

import com.finance.bank.exception.InsufficientAmountException;
import com.finance.bank.exception.InvalidAccountException;
import com.finance.bank.exception.InvalidAmountException;
import com.finance.bank.util.AccountValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract public class Account {
    private static final int ACCOUNT_NUMBER_LENGTH = 16;
    private static final BigDecimal WITHDRAW_FEE_PERCENT = BigDecimal.valueOf(0.01); // 0.01%

    // CRITICAL: Constants for BigDecimal scale normalization
    // This ensures all amounts have exactly 2 decimal places (e.g., 500.00 not 500)
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final Customer owner;
    private final AccountType accountType;
    protected BigDecimal balance;
    private final List<Transaction> transactions = new ArrayList<>();
    private final String accountNumber;

    /**
     * Normalizes BigDecimal amount to consistent scale (2 decimal places)
     * This prevents test failures where: expected 500.00 but was 500
     *
     * @param amount Amount to normalize
     * @return Amount with exactly 2 decimal places
     */
    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }
        return amount.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Constructor with composition relationship & validation
     */
    protected Account(String accountNumber, Customer owner, AccountType accountType)
            throws InvalidAccountException {
        this.accountType = accountType;

        // Validate (throw on error)
        AccountValidator.validateAccountNumber(accountNumber);
        AccountValidator.validateOwner(owner);

        this.accountNumber = accountNumber;
        this.owner = owner;
        // CRITICAL: Initialize with normalized zero (0.00)
        this.balance = normalizeAmount(BigDecimal.ZERO);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    /**
     * Returns account balance with consistent 2 decimal places
     * CRITICAL for test compatibility
     */
    public BigDecimal getBalance() {
        return balance;
    }

    public Customer getOwner() {
        return owner;
    }

    /**
     * Increases account balance by the given amount.
     *
     * ⚠️ This method ONLY mutates account state (balance).
     * ⚠️ It does NOT create or record a Transaction.
     *
     * Transaction creation, employee context, and audit
     * are handled by TransactionService.
     *
     * @param amount amount to deposit (must be positive)
     * @throws InvalidAmountException if amount is null or <= 0
     */
    public void deposit(BigDecimal amount) throws InvalidAmountException {
        // Validate business invariant: amount must be positive
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Invalid deposit amount");
        }

        // CRITICAL: Normalize the result to maintain consistent scale
        this.balance = normalizeAmount(this.balance.add(amount));
    }

    /**
     * Abstract method - implemented differently by SavingsAccount and CurrentAccount
     */
    public abstract void withdraw(BigDecimal amount)
            throws InvalidAmountException, InsufficientAmountException;

    /**
     * Protected setter for balance - used by child classes
     * CRITICAL: Always normalizes to maintain 2 decimal places
     */
    protected void setBalance(BigDecimal newBalance) {
        this.balance = normalizeAmount(newBalance);
    }

    // Helper for printing
    public String getTypeName() {
        return accountType.label();
    }

    public static int getAccountNumberLength() {
        return ACCOUNT_NUMBER_LENGTH;
    }

    public static BigDecimal getWithdrawFeePercent() {
        return WITHDRAW_FEE_PERCENT;
    }

    /**
     * Protected method to be used by child classes to record transactions only
     */
    protected void recordTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    /**
     * Returns unmodifiable list of transactions - cannot be changed from outside
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

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

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", ownerNationalId=" + (owner != null ? owner.getNationalId() : "null") +
                '}';
    }
}