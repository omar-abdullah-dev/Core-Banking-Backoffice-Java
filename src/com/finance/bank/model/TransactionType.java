package com.finance.bank.model;

public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER("Transfer");

    private final String label;
    TransactionType(String label) { this.label = label; }
    @Override
    public String toString() {
        return label;
    }
}
