package com.finance.bank.exception;

public class InvalidAmountException extends  Exception{
    InvalidAmountException(){}
    public InvalidAmountException(String msg){
        super(msg);
    }
}
