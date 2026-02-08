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
    public void withdraw(BigDecimal amount) throws InvalidAmountException, InsufficientAmountException {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Amount must be greater than 0");
        }

        BigDecimal feeAmount = amount.multiply(Account.getWithdrawFeePercent()) ;
        BigDecimal total = amount.add(feeAmount);
        BigDecimal validatedBalance = balance.subtract(total);


        if (validatedBalance .compareTo( overdraftLimit.multiply(BigDecimal.valueOf(-1))) < 0) {
            throw new InsufficientAmountException("Overdraft limit exceeded");
        }
        balance = validatedBalance;
        Transaction transaction = new Transaction(TransactionType.WITHDRAWAL,amount,feeAmount,balance);
        recordTransaction(transaction);

    }

}
