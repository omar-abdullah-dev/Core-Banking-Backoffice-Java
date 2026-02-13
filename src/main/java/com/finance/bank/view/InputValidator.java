package com.finance.bank.view;

import com.finance.bank.exception.InvalidNationalIdException;
import com.finance.bank.util.NationalIdValidator;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Utility class for validating and reading user input
 */
public class InputValidator {
    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Reads and validates National ID input
     * @return Valid National ID or null if validation fails
     */
    public String readAndValidateNationalId() {
        System.out.print("Enter Customer National ID: ");
        String nationalId = scanner.nextLine().trim();
        
        try {
            NationalIdValidator.validateNationalId(nationalId);
            return nationalId;
        } catch (InvalidNationalIdException e) {
            System.out.println("[!] " + e.getMessage());
            return null;
        }
    }

    /**
     * Reads and validates BigDecimal input (amount)
     * @param prompt The prompt to display to user
     * @return Valid BigDecimal or null if user cancels or input is invalid
     */
    public BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            // Allow user to cancel operation
            if (input.equalsIgnoreCase("q")) {
                System.out.println("[!] Operation cancelled.");
                return null;
            }

            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid amount format. Please enter a valid number or 'q' to cancel.");
            }
        }
    }

    /**
     * Reads integer input with validation
     * @param prompt The prompt to display to user
     * @param min Minimum valid value
     * @param max Maximum valid value
     * @return Valid integer or null if validation fails
     */
    public Integer readInteger(String prompt, int min, int max) {
        System.out.print(prompt);
        
        try {
            int value = Integer.parseInt(scanner.nextLine().trim());
            
            if (value < min || value > max) {
                System.out.printf("[!] Please enter a number between %d and %d%n", min, max);
                return null;
            }
            
            return value;
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid input. Please enter a valid number.");
            return null;
        }
    }

    /**
     * Reads a non-empty string input
     * @param prompt The prompt to display to user
     * @return Non-empty trimmed string
     */
    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            
            if (!input.isEmpty()) {
                return input;
            }
            
            System.out.println("[!] Input cannot be empty. Please try again.");
        }
    }

    /**
     * Reads confirmation (yes/no)
     * @param prompt The prompt to display to user
     * @return true for yes, false for no
     */
    public boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            }
            
            System.out.println("[!] Please enter 'y' for yes or 'n' for no.");
        }
    }
}
