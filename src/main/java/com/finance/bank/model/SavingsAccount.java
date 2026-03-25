package com.finance.bank.model;

import com.finance.bank.exception.InsufficientAmountException;
import com.finance.bank.exception.InvalidAccountException;
import com.finance.bank.exception.InvalidAmountException;

import java.math.BigDecimal;

public class SavingsAccount extends Account {

    public SavingsAccount(String accountNumber, Customer owner) throws InvalidAccountException {
        super(accountNumber, owner, AccountType.SAVINGS);
    }

    @Override
    public void withdraw(BigDecimal amount)
            throws InvalidAmountException, InsufficientAmountException {

        // Validation: amount must be positive
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than 0");
        }

        // Calculate withdrawal fee
        BigDecimal feeAmount = amount.multiply(Account.getWithdrawFeePercent());
        BigDecimal total = amount.add(feeAmount);

        // Savings accounts do NOT allow overdraft
        if (balance.compareTo(total) < 0) {
            throw new InsufficientAmountException("Insufficient funds");
        }

        // State mutation only
        setBalance(this.balance.subtract(total));
    }


}