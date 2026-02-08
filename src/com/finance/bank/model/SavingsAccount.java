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
    public void withdraw(BigDecimal amount) throws InsufficientAmountException, InvalidAmountException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) // negative or zero amount logically is not accepted
        {
            throw new InvalidAmountException("Amount must be greater than 0");
        }
        BigDecimal feeAmount =amount.multiply(Account.getWithdrawFeePercent());
        BigDecimal total = amount.add(feeAmount);
        if (balance.compareTo(total) < 0) { // the total must be less than balance
            throw new InsufficientAmountException("Insufficient funds");
        }
//            implement subtraction here:
            balance = balance.subtract(total);
            Transaction transaction = new Transaction(TransactionType.WITHDRAWAL,amount,feeAmount,balance);
            recordTransaction(transaction);

    }


}