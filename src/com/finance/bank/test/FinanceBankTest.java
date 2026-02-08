package com.finance.bank.test;

import com.finance.bank.exception.InsufficientAmountException;
import com.finance.bank.exception.InvalidAmountException;

import static com.finance.bank.util.NumberFormatter.amountFormatter;

public class FinanceBankTest {
    private double balance;
    private static final double WITHDRAW_FEE_PERCENT = 0.01;


/*
    public void setName(String name) {
        this.name = name;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }
*/

    public FinanceBankTest() {}

/*
    public FinanceBankTest(String name, String nationalId, String email) {
        this.name = name;
        this.nationalId = nationalId;
        this.email = email;
    }
*/


//   Testing: balance enquiry method
    public void balanceEnquiry() {
        System.out.println("Current balance : " + amountFormatter(balance));
    }
//   Testing: deposit method with exception handling
    public void deposit(double amount) throws InvalidAmountException {
        if (amount<=0)
        {
           throw new InvalidAmountException("Invalid amount");
        }
        balance = balance + amount;
        System.out.println("Deposited : " + amountFormatter(amount));
        this.balanceEnquiry();
        System.out.println();

    }
//   Testing: withdraw method with fee and exception handling
    public void withdraw(double amount) throws InsufficientAmountException, InvalidAmountException {
        double feeAmount = amount * WITHDRAW_FEE_PERCENT;
        double total = amount+feeAmount;
        if (total<=0)
        {
            throw new InvalidAmountException("Amount must be greater than 0");
        } else if (total>balance) {
            throw new InsufficientAmountException("Insufficient funds");
        }
        else {
            balance -= total;
            System.out.println("withdrawn : " + amountFormatter(amount));
            System.out.println("Fee : " + amountFormatter(feeAmount));
            System.out.println("Total out : "+amountFormatter(amount) + " Will be withdrawn\n");

//            void df.format(amount);
        }
         this.balanceEnquiry();
         System.out.println();
    }
//    Testing: notifyMail method (empty for now)
    private void notifyMail(){}
}
