package com.finance.bank.util;

public class PasswordValidator {

    private PasswordValidator() {}

    public static void validate(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException(
                    "Password must be at least 6 characters long"
            );
        }
    }
}
