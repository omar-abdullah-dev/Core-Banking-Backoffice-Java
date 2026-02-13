package com.finance.bank.view;

import com.finance.bank.model.Employee;
import com.finance.bank.service.AuthenticationService;

import java.util.Scanner;

/**
 * Handles employee login and authentication display logic
 */
public class LoginView {
    private final Scanner scanner;
    private final AuthenticationService authService;

    public LoginView(Scanner scanner, AuthenticationService authService) {
        this.scanner = scanner;
        this.authService = authService;
    }

    /**
     * Handles the login process until successful authentication
     * @return Authenticated Employee
     */
    public Employee handleLogin() {
        Employee employee = null;
        
        while (employee == null) {
            System.out.println("\n========================================");
            System.out.println("         Finance Bank - Login");
            System.out.println("========================================");
            
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            try {
                employee = authService.login(username, password);
                displayLoginSuccess(employee);
            } catch (Exception e) {
                displayLoginError(e.getMessage());
            }
        }
        
        return employee;
    }

    private void displayLoginSuccess(Employee employee) {
        System.out.println("\n✓ Login Successful!");
        System.out.println("========================================");
        System.out.printf("Logged in as: %s%n", employee.getUserName());
        System.out.printf("Role: %s%n", employee.getRole());
        System.out.println("========================================\n");
    }

    private void displayLoginError(String message) {
        System.out.println("\n[!] Login Failed: " + message);
        System.out.println("Please try again.\n");
    }
}
