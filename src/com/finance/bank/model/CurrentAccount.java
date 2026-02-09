package com.finance.bank.model;

import com.finance.bank.exception.InsufficientAmountException;
import com.finance.bank.exception.InvalidAccountException;
import com.finance.bank.exception.InvalidAmountException;

import java.math.BigDecimal;

public class CurrentAccount extends Account{

    private BigDecimal overdraftLimit;

    public CurrentAccount(String accountNumber, Customer owner, BigDecimal overdraftLimit) throws InvalidAccountException {
        super(accountNumber, owner, AccountType.CURRENT);
        this.overdraftLimit = overdraftLimit;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(BigDecimal amount)
            throws InvalidAmountException, InsufficientAmountException {

        // Validation: amount must be positive
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than 0");
        }

        // Calculate withdrawal fee and resulting balance, validating against overdraft limit
        // State mutation only (no transaction creation here)
        this.balance = getResultingBalance(amount);;
    }

    private BigDecimal getResultingBalance(BigDecimal amount) throws InsufficientAmountException {
        BigDecimal feeAmount = amount.multiply(Account.getWithdrawFeePercent());
        BigDecimal total = amount.add(feeAmount);

        // Validate against overdraft limit
        // allowed minimum balance = -overdraftLimit
        BigDecimal minAllowedBalance = overdraftLimit.negate();
        BigDecimal resultingBalance = balance.subtract(total);

        if (resultingBalance.compareTo(minAllowedBalance) < 0) {
            throw new InsufficientAmountException("Overdraft limit exceeded");
        }
        return resultingBalance;
    }

}
