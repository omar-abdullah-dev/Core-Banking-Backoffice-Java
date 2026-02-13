package com.finance.bank.exception;

public class DuplicateAccountException extends  Exception {
    public DuplicateAccountException(String message) {
        super(message);
    }
}
