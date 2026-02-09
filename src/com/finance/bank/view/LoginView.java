package com.finance.bank.view;

import com.finance.bank.model.Employee;
import com.finance.bank.service.AuthenticationService;

import java.util.Scanner;

public class LoginView {

    private final AuthenticationService authenticationService;
    private final Scanner scanner;

    public LoginView(AuthenticationService authenticationService, Scanner scanner) {
        this.authenticationService = authenticationService;
        this.scanner = scanner;
    }

    /**
     * Displays login screen and blocks until login succeeds
     */
    public Employee show() {
        System.out.println("=================================");
        System.out.println("      Banking System Login       ");
        System.out.println("=================================");

        while (true) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            Employee employee = authenticationService.login(username, password);

            if (employee != null) {
                System.out.println();
                System.out.println("Login successful.");
                System.out.println(STR."Welcome, \{employee.getName()} (\{employee.getRole()})");
                System.out.println();
                return employee;
            }

            System.out.println("Invalid username or password. Please try again.\n");
        }
    }
    static void main(String[] args) {

        AuthenticationService authenticationService = new AuthenticationService();
        Scanner scanner = new Scanner(System.in);
        LoginView loginView = new LoginView(authenticationService, scanner);
        loginView.show();
    }

}
