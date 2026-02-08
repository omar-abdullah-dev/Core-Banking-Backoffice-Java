package com.finance.bank.model;

public enum AccountType {
    SAVINGS("Savings"),
    CURRENT("Current");

    private final String label;
    AccountType(String label) { this.label = label; }
    public String label() { return label; }
}
