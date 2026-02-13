package com.finance.bank.model;

// Task 1.1: Define employee roles

public enum Role {
      CS("Customer Service"), // Customer Service
      TELLER ("Teller"), //  Teller / Cashier
      MANAGER("Manager");// Branch Manager
//    ADMIN // System Administrator (if needed for future expansion)
    private final String label;
    Role(String label) { this.label = label; }
    @Override
    public final String toString() {
        return label;
    }
}
