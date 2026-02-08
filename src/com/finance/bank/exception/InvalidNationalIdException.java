package com.finance.bank.exception;

public class InvalidNationalIdException extends RuntimeException {

    public InvalidNationalIdException(String message) {
        super(message);
    }

    public InvalidNationalIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
