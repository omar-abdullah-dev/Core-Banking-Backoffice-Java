package com.finance.bank.model;

import com.finance.bank.util.IdGenerator;

import java.math.BigDecimal;
import java.time.Instant;
/**
 * Represents a financial record of an account operation
 * (e.g. deposit, withdrawal).

 * Transactions are part of the bank's audit and accounting system
 * and are valid in both employee-assisted and automated operations.
 */

public class Transaction {

    private final String transactionId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal fee;
    private final BigDecimal total;
    private final Instant timestamp;
    private final BigDecimal balanceAfter;
    private final String accountNumber;


    // Audit
    private final String performedByEmployeeId;
    private final String performedByEmployeeName;
    private final Role performedByRole;

    public Transaction(TransactionType type,
                       BigDecimal amount,
                       BigDecimal fee,
                       BigDecimal balanceAfter,
                       Account account,
                       Employee employee) {

        this.transactionId = IdGenerator.generateTransactionId();
        this.type = type;
        this.amount = amount;
        this.fee = fee;
        this.total = amount.add(fee);
        this.balanceAfter = balanceAfter;
        this.timestamp = Instant.now();

        // Account context
        this.accountNumber = account.getAccountNumber();

        // Audit
        this.performedByEmployeeId = employee.getSystemId();
        this.performedByEmployeeName = employee.getName();
        this.performedByRole = employee.getRole();
    }


    public String getTransactionId() {
        return transactionId;
    }
    public TransactionType getType() {
        return type;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public BigDecimal getFee() {
        return fee;
    }
    public BigDecimal getTotal() {
        return total;
    }
    public Instant getTimestamp() {
        return timestamp;
    }
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public String getPerformedByEmployeeId() {
        return performedByEmployeeId;
    }

    public String getPerformedByEmployeeName() {
        return performedByEmployeeName;
    }
    public Role getPerformedByRole() {
        return performedByRole;
    }


}
