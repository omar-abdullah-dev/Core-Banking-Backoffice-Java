package com.finance.bank.view;

import com.finance.bank.model.Customer;
import com.finance.bank.model.Employee;
import com.finance.bank.service.BankService;

import java.util.List;
import java.util.Scanner;

/**
 * Handles customer-related display and input operations
 */
public class CustomerView {
    private final Scanner scanner;
    private final BankService bankService;
    private final InputValidator inputValidator;

    public CustomerView(Scanner scanner, BankService bankService) {
        this.scanner = scanner;
        this.bankService = bankService;
        this.inputValidator = new InputValidator(scanner);
    }

    /**
     * Handles the customer creation process
     * @param employee Currently logged-in employee
     */
    public void handleCreateCustomer(Employee employee) {
        if (!validateEmployeeLogin(employee)) {
            return;
        }

        System.out.println("\n===== Create New Customer =====");
        
        System.out.print("Enter Customer Name: ");
        String name = scanner.nextLine().trim();

        String nationalId = inputValidator.readAndValidateNationalId();
        if (nationalId == null) {
            return;
        }

        try {
            Customer customer = bankService.createCustomer(employee, name, nationalId);
            displayCustomerCreated(customer);
        } catch (Exception e) {
            displayError("Failed to create customer: " + e.getMessage());
        }
    }

    /**
     * Displays list of all customers
     */
    public void handleShowCustomers() {
        List<Customer> customers = bankService.getCustomers();
        
        if (customers.isEmpty()) {
            System.out.println("\n[!] No customers registered yet.");
            return;
        }

        System.out.println("\n========== Customers List ==========");
        for (Customer customer : customers) {
            System.out.printf("Name: %s | National ID: %s | Accounts: %d%n",
                    customer.getName(),
                    customer.getNationalId(),
                    customer.getAccounts().size());
        }
        System.out.println("====================================");
    }

    /**
     * Finds and returns a customer by National ID
     * @return Customer if found, null otherwise
     */
    public Customer findCustomerByNationalId() {
        String nationalId = inputValidator.readAndValidateNationalId();
        if (nationalId == null) {
            return null;
        }

        Customer customer = bankService.findCustomerByNationalId(nationalId);
        if (customer == null) {
            System.out.println("[!] Customer not found.");
            return null;
        }

        return customer;
    }

    private void displayCustomerCreated(Customer customer) {
        System.out.println("\n===== Customer Created Successfully =====");
        System.out.printf("System ID     : %s%n", customer.getSystemId());
        System.out.printf("Customer Name : %s%n", customer.getName());
        System.out.printf("National ID   : %s%n", customer.getNationalId());
        System.out.println("=========================================");
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
