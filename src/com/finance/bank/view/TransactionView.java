package com.finance.bank.view;

import com.finance.bank.model.Account;
import com.finance.bank.model.Customer;
import com.finance.bank.model.Employee;
import com.finance.bank.model.Transaction;
import com.finance.bank.service.BankService;
import com.finance.bank.util.TransactionPrinter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import static com.finance.bank.util.NumberFormatter.timeFormatter;

/**
 * Handles transaction-related display and input operations
 */
public class TransactionView {
    private final Scanner scanner;
    private final BankService bankService;
    private final InputValidator inputValidator;
    private final CustomerView customerView;
    private final AccountView accountView;

    public TransactionView(Scanner scanner, BankService bankService) {
        this.scanner = scanner;
        this.bankService = bankService;
        this.inputValidator = new InputValidator(scanner);
        this.customerView = new CustomerView(scanner, bankService);
        this.accountView = new AccountView(scanner, bankService);
    }

    /**
     * Handles deposit operation
     * @param employee Currently logged-in employee
     */
    public void handleDeposit(Employee employee) {
        if (!validateEmployeeLogin(employee)) {
            return;
        }

        Account account = getValidatedAccount();
        if (account == null) {
            return;
        }

        BigDecimal amount = inputValidator.readBigDecimal("Enter deposit amount: ");
        if (amount == null) {
            return;
        }

        try {
            bankService.deposit(employee, account.getAccountNumber(), amount);
            displayTransactionSuccess("Deposit", account.getBalance());
        } catch (Exception e) {
            displayError("Deposit failed: " + e.getMessage());
        }
    }

    /**
     * Handles withdrawal operation
     * @param employee Currently logged-in employee
     */
    public void handleWithdraw(Employee employee) {
        if (!validateEmployeeLogin(employee)) {
            return;
        }

        Account account = getValidatedAccount();
        if (account == null) {
            return;
        }

        BigDecimal amount = inputValidator.readBigDecimal("Enter withdrawal amount: ");
        if (amount == null) {
            return;
        }

        try {
            bankService.withdraw(employee, account.getAccountNumber(), amount);
            displayTransactionSuccess("Withdrawal", account.getBalance());
        } catch (Exception e) {
            displayError("Withdrawal failed: " + e.getMessage());
        }
    }

    /**
     * Displays transaction history for a selected account
     */
    public void handleTransactionHistory() {
        Customer customer = customerView.findCustomerByNationalId();
        if (customer == null) {
            return;
        }

        Account account = accountView.chooseAccountFromCustomer(customer);
        if (account == null) {
            return;
        }

        List<Transaction> transactions = bankService.getTransactionsByAccount(account.getAccountNumber());
        
        if (transactions.isEmpty()) {
            System.out.println("[!] No transactions found.");
            return;
        }

        displayTransactionHistory(account, transactions);
    }

    /**
     * Exports transaction history to CSV file
     */
    public void handleExportTransactions() {
        Customer customer = customerView.findCustomerByNationalId();
        if (customer == null) {
            return;
        }

        Account account = accountView.chooseAccountFromCustomer(customer);
        if (account == null) {
            return;
        }

        List<Transaction> transactions = bankService.getTransactionsByAccount(account.getAccountNumber());

        if (transactions.isEmpty()) {
            System.out.println("[!] No transactions to export.");
            return;
        }

        try {
            TransactionPrinter.exportNewTransactions(account.getAccountNumber(), transactions);
            System.out.println("✓ Transactions exported successfully");
        } catch (Exception e) {
            displayError("Export failed: " + e.getMessage());
        }
    }

    private Account getValidatedAccount() {
        Customer customer = customerView.findCustomerByNationalId();
        if (customer == null) {
            return null;
        }

        return accountView.chooseAccountFromCustomer(customer);
    }

    private void displayTransactionHistory(Account account, List<Transaction> transactions) {
        System.out.println("\n===== Transaction History =====");
        System.out.printf("Account Type: %s%n", account.getAccountType().label());
        System.out.println("--------------------------------");

        transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .forEach(this::displayTransaction);

        System.out.println("================================");
    }

    private void displayTransaction(Transaction transaction) {
        System.out.printf("%s | Amount: %s | Balance After: %s | By: %s (%s) | %s | %s%n",
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getPerformedByEmployeeName(),
                transaction.getPerformedByRole(),
                timeFormatter(transaction.getTimestamp()),
                transaction.getTransactionId());
    }

    private void displayTransactionSuccess(String operationType, BigDecimal newBalance) {
        System.out.println("✓ " + operationType + " successful");
        System.out.println("New Balance: " + newBalance);
    }

    private void displayError(String message) {
        System.out.println("[!] " + message);
    }

    private boolean validateEmployeeLogin(Employee employee) {
        if (employee == null) {
            System.out.println("[!] Please login first");
            return false;
        }
        return true;
    }
}
