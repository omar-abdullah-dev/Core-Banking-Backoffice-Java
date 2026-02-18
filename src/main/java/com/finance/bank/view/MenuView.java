package com.finance.bank.view;

import com.finance.bank.model.Employee;
import com.finance.bank.model.EmployeeRole;

import java.util.Scanner;

/**
 * Handles menu display and choice input based on employee role
 */
public class MenuView {
    private final Scanner scanner;

    public MenuView(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Displays role-based menu for the logged-in employee
     * @param employee Currently logged-in employee
     */
    public void displayMenu(Employee employee) {
        System.out.println("\n----------------------------------------");
        System.out.println("     Finance Bank - Main Menu");
        System.out.println("----------------------------------------");
        System.out.printf("Logged in as: %s (%s)%n", employee.getUserName(), employee.getRole());
        System.out.println("----------------------------------------");

        EmployeeRole role = employee.getRole();

        switch (role) {
            case CS -> displayCustomerServiceMenu();
            case TELLER -> displayTellerMenu();
            case MANAGER -> displayManagerMenu();
        }

        System.out.println("----------------------------------------");
    }

    private void displayCustomerServiceMenu() {
        System.out.println("1. Create Customer");
        System.out.println("2. Add Account");
        System.out.println("3. Show Customers");
        System.out.println();
        System.out.println("9. Logout");
        System.out.println("0. Exit");
    }

    private void displayTellerMenu() {
        System.out.println("5. Deposit");
        System.out.println("6. Withdraw");
        System.out.println("7. Show Transaction History");
        System.out.println();
        System.out.println("9. Logout");
        System.out.println("0. Exit");
    }

    private void displayManagerMenu() {
        System.out.println("1. Create Customer");
        System.out.println("2. Add Account");
        System.out.println("3. Show Customers");
        System.out.println("4. Show Accounts By National ID");
        System.out.println();
        System.out.println("5. Deposit");
        System.out.println("6. Withdraw");
        System.out.println("7. Show Transaction History");
        System.out.println("8. Export Transaction History to Excel (CSV)");
        System.out.println();
        System.out.println("9. Logout");
        System.out.println("0. Exit");
    }

    /**
     * Gets user's menu choice
     * @return User's choice as String
     */
    public String getChoice() {
        System.out.print("Enter your choice: ");
        return scanner.nextLine().trim();
    }
}
