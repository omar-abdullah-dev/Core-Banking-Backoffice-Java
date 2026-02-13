package com.finance.bank.view;

import com.finance.bank.model.*;
import com.finance.bank.service.BankService;
import com.finance.bank.util.IdGenerator;
import com.finance.bank.util.NumberFormatter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Handles account-related display and input operations
 */
public class AccountView {
    private final Scanner scanner;
    private final BankService bankService;
    private final InputValidator inputValidator;
    private final CustomerView customerView;

    public AccountView(Scanner scanner, BankService bankService) {
        this.scanner = scanner;
        this.bankService = bankService;
        this.inputValidator = new InputValidator(scanner);
        this.customerView = new CustomerView(scanner, bankService);
    }

    /**
     * Handles the account creation process
     * @param employee Currently logged-in employee
     */
    public void handleAddAccount(Employee employee) {
        if (!validateEmployeeLogin(employee)) {
            return;
        }

        System.out.println("\n===== Add New Account =====");
        System.out.print("Enter Account Type (1 for Savings, 2 for Current): ");
        String accountType = scanner.nextLine().trim();

        Customer customer = customerView.findCustomerByNationalId();
        if (customer == null) {
            return;
        }

        String accountNumber = IdGenerator.generateAccountNumber();
        System.out.println("Generated Account Number: " + NumberFormatter.mask(accountNumber, 4));

        try {
            switch (accountType) {
                case "1" -> createSavingsAccount(employee, customer, accountNumber);
                case "2" -> createCurrentAccount(employee, customer, accountNumber);
                default -> System.out.println("[!] Invalid account type.");
            }
        } catch (Exception e) {
            displayError("Failed to create account: " + e.getMessage());
        }
    }

    /**
     * Displays all accounts for a customer by National ID
     */
    public void handleShowAccountsByNationalId() {
        Customer customer = customerView.findCustomerByNationalId();
        if (customer == null) {
            return;
        }

        System.out.printf("\nCustomer: %s%n", customer.getName());

        List<Account> accounts = customer.getAccounts();
        if (accounts.isEmpty()) {
            System.out.println("[!] No accounts found for this customer.");
            return;
        }

        System.out.println("Accounts:");
        for (Account account : accounts) {
            System.out.printf("- %s | %s%n",
                    NumberFormatter.mask(account.getAccountNumber(), 4),
                    account.getAccountType().label());
        }
    }

    /**
     * Allows user to select an account from a customer's accounts
     * @param customer The customer whose accounts to choose from
     * @return Selected Account or null
     */
    public Account chooseAccountFromCustomer(Customer customer) {
        List<Account> accounts = customer.getAccounts();

        if (accounts.isEmpty()) {
            System.out.println("[!] Customer has no accounts.");
            return null;
        }

        // Auto-select if only one account
        if (accounts.size() == 1) {
            System.out.println(accounts.get(0).getAccountType().label() + " Account auto-selected.");
            return accounts.get(0);
        }

        // Display multiple accounts for selection
        System.out.println("\nCustomer Accounts:");
        for (int i = 0; i < accounts.size(); i++) {
            System.out.printf("%d) %s | Account Number: %s%n",
                    i + 1,
                    accounts.get(i).getAccountType().label(),
                    NumberFormatter.mask(accounts.get(i).getAccountNumber(), 4));
        }

        System.out.print("Choose account number: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice < 1 || choice > accounts.size()) {
                System.out.println("[!] Invalid selection.");
                return null;
            }

            return accounts.get(choice - 1);
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid input.");
            return null;
        }
    }

    private void createSavingsAccount(Employee employee, Customer customer, String accountNumber) throws Exception {
        SavingsAccount savingsAccount = new SavingsAccount(accountNumber, customer);
        bankService.openAccount(employee, savingsAccount);
        System.out.println("✓ Savings Account Created Successfully");
    }

    private void createCurrentAccount(Employee employee, Customer customer, String accountNumber) throws Exception {
        BigDecimal overdraftLimit = inputValidator.readBigDecimal("Enter Overdraft Limit: ");
        if (overdraftLimit == null) {
            return;
        }

        CurrentAccount currentAccount = new CurrentAccount(accountNumber, customer, overdraftLimit);
        bankService.openAccount(employee, currentAccount);
        System.out.println("✓ Current Account Created Successfully");
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