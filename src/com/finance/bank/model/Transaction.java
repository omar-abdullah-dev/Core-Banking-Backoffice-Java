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



    public  Transaction( TransactionType type, BigDecimal amount, BigDecimal fee, BigDecimal balanceAfter) {
        this.transactionId = IdGenerator.generateTransactionId();
        this.type = type;
        this.amount = amount;
        this.fee= fee;
        this.total = amount.add(fee);
        this.balanceAfter = balanceAfter;
        this.timestamp = Instant.now();
    }
    public String getTransactionId() {
        return transactionId;
    }
    public String getType() {
        return type.toString();
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
}
