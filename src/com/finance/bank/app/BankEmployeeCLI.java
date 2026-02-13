package com.finance.bank.app;

import com.finance.bank.model.Employee;
import com.finance.bank.service.AuthenticationService;
import com.finance.bank.service.BankService;
import com.finance.bank.view.*;

import java.util.Scanner;

public class BankEmployeeCLI {
    private static Employee currentEmployee;
    private static final AuthenticationService authService = new AuthenticationService();
    private static final BankService bankService = BankService.getInstance();

    // Views
    private static LoginView loginView;
    private static MenuView menuView;
    private static CustomerView customerView;
    private static AccountView accountView;
    private static TransactionView transactionView;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Initialize views
        initializeViews(scanner);

        // Handle login
        currentEmployee = loginView.handleLogin();

        // Main application loop
        while (true) {
            menuView.displayMenu(currentEmployee);

            String choice = menuView.getChoice();

            if (!handleMenuChoice(choice, scanner)) {
                break; // Exit application
            }
        }

        scanner.close();
    }

    private static void initializeViews(Scanner scanner) {
        loginView = new LoginView(scanner, authService);
        menuView = new MenuView(scanner);
        customerView = new CustomerView(scanner, bankService);
        accountView = new AccountView(scanner, bankService);
        transactionView = new TransactionView(scanner, bankService);
    }

    private static boolean handleMenuChoice(String choice, Scanner scanner) {
        try {
            switch (choice) {
                // Customer Service & Manager
                case "1" -> customerView.handleCreateCustomer(currentEmployee);
                case "2" -> accountView.handleAddAccount(currentEmployee);
                case "3" -> customerView.handleShowCustomers();

                // Manager only
                case "4" -> accountView.handleShowAccountsByNationalId();

                // Teller & Manager
                case "5" -> transactionView.handleDeposit(currentEmployee);
                case "6" -> transactionView.handleWithdraw(currentEmployee);
                case "7" -> transactionView.handleTransactionHistory();

                // Manager only
                case "8" -> transactionView.handleExportTransactions();

                // Session control
                case "9" -> {
                    handleLogout();
                    currentEmployee = loginView.handleLogin();
                }
                case "0" -> {
                    handleExit();
                    return false;
                }

                default -> System.out.println("[!] Invalid choice. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("[!] Error: " + e.getMessage());
        }

        return true;
    }

    private static void handleLogout() {
        System.out.println("✓ Logged out.");
        currentEmployee = null;
    }

    private static void handleExit() {
        System.out.println("\nThank you for using Finance Bank. Exiting...");
    }
}
