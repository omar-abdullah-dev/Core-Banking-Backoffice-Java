package com.finance.bank.util;

import com.finance.bank.exception.InvalidAccountException;
import com.finance.bank.model.Account;
import com.finance.bank.model.Customer;

public final class AccountValidator {
    private AccountValidator() {}

    public static void validateAccountNumber(String accountNumber) throws InvalidAccountException {

        if (accountNumber == null) {
            throw new InvalidAccountException("Account number cannot be null");
        }

        accountNumber = accountNumber.trim();

        if (accountNumber.isBlank()) {
            throw new InvalidAccountException("Account number cannot be Empty or Blank");
        }

        if (accountNumber.length() != Account.getAccountNumberLength()) {
            throw new InvalidAccountException("Account number must be exactly " + Account.getAccountNumberLength() + " digits");
        }

        if (!accountNumber.matches("\\d{" + Account.getAccountNumberLength() + "}")) {
            throw new InvalidAccountException("Account number must contain digits only");
        }

      final String expectedPrefix = IdGenerator.getBankCode() + IdGenerator.getBranchCode();
        if (!accountNumber.startsWith(expectedPrefix)) {
            throw new InvalidAccountException(
                    "Account number must start with bank prefix " + expectedPrefix
            );
        }
    }

    public static void validateOwner(Customer owner) throws InvalidAccountException {
        if (owner == null) throw new InvalidAccountException("Owner cannot be null");
        if (owner.getNationalId() == null || owner.getNationalId().isBlank())
            throw new InvalidAccountException("Owner must have a valid national id");
    }

}
