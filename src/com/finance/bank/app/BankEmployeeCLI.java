package com.finance.bank.app;

import com.finance.bank.exception.InvalidNationalIdException;
import com.finance.bank.model.*;
import com.finance.bank.service.AuthenticationService;
import com.finance.bank.service.BankService;
import com.finance.bank.util.IdGenerator;
import com.finance.bank.util.NationalIdValidator;
import com.finance.bank.util.NumberFormatter;
import com.finance.bank.util.TransactionPrinter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import static com.finance.bank.util.NumberFormatter.timeFormatter;

public class BankEmployeeCLI {
    private static Employee currentEmployee;
    private static final AuthenticationService authService =
            new AuthenticationService();
    private static final BankService bankService = BankService.getInstance();
//TODO: Add Employee Login & Roles (Teller, Manager) → control access to certain operations
// (e.g. only manager can create customers/accounts).

// TODO: Define employee roles
// - CS
// - TELLER
// - MANAGER
// TODO: Create Employee model
// - id
// - username
// - password
// - name
// - role (CS / TELLER / MANAGER)
// - password verification method
// TODO: Handle employee authentication
// - Create AuthService
// - Store employees with assigned roles (hardcoded)
// - Implement login(username, password) -> Employee | null
// TODO: Require employee login before showing main menu
// - Show login screen at startup
// - Block access until login succeeds
// TODO: Store logged-in employee in session
// - currentEmployee variable
// - Display "Logged in as: <name> (<role>)"
// TODO: Show menu options based on employee role
// - CS: create customer, add account
// - Teller: deposit, withdraw, view transactions
// - Manager: all options
// TODO: Enforce role-based access in service layer
// - Validate role before executing operation
// - Throw AccessDeniedException or show error message
    // only CS or MANAGER can create customer
// TODO: Track which employee performed the transaction
// - Extend Transaction model
// - Add field: performedBy (employee name or id)
// TODO: Include employee context when performing transactions
// - Pass currentEmployee to deposit / withdraw
// - Save employee info in Transaction
// TODO: Print transaction with employee information
// - Show employee name and role
// TODO: Add employee info to CSV export
// - Add columns: PerformedBy, Role
// - Keep export incremental (no duplicates)
// TODO: Allow employee logout
// - Add logout option
// - Clear currentEmployee
// - Redirect to login screen
// TODO: Prevent operations without login
// - Check currentEmployee != null
// - Block all system actions otherwise

    /*
    * Implementation Sequence:
        🟢 Recommended Execution Order
            1️⃣ Role enum
            2️⃣ Employee model
            3️⃣ AuthService
            4️⃣ Login flow
            5️⃣ Session handling
            6️⃣ Role-based menu
            7️⃣ Role enforcement
            8️⃣ Transaction ↔ Employee
            9️⃣ Printer + CSV
            🔟 Logout & safety
    */

    private static void handleLogin(Scanner in) {
        while (currentEmployee == null) {
            System.out.print("Username: ");
            String username = in.nextLine();

            System.out.print("Password: ");
            String password = in.nextLine();

            try {
                currentEmployee = authService.login(username, password);
                System.out.println(
                        "✓ Logged in as "
                                + currentEmployee.getUserName()
                                + " (" + currentEmployee.getRole() + ")"
                );
            } catch (Exception e) {
                System.out.println("[!] " + e.getMessage());
            }
        }
    }

    private static void handleLogout(Scanner in) {
        System.out.println("✓ Logged out.");
        currentEmployee = null;
        handleLogin(in);
    }


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        handleLogin(in);
        while (true) {

            // Print menu based on current employee role
            printMenu();

            System.out.print("Enter your choice: ");
            String choice = in.nextLine().trim();

            /*
             * Menu Options (Role-based visibility):
             *  0 - Exit
             * CS:
             *  1 - Create Customer
             *  2 - Add Account
             *  3 - Show Customers
             *
             * TELLER:
             *  5 - Deposit
             *  6 - Withdraw
             *  7 - Transaction History
             *
             * MANAGER:
             *  All options below
             *
             * Common:
             *  9 - Logout
             */

            switch (choice) {

                // 🧑‍💼 Customer Service & Manager
                case "1" -> handleCreateCustomer(in);           // CS, MANAGER
                case "2" -> handleAddAccount(in);               // CS, MANAGER
                case "3" -> handleShowCustomers();              // CS, MANAGER

                // 👑 Manager only
                case "4" -> handleShowAccountsByNationalId(in); // MANAGER

                // 💵 Teller & Manager
                case "5" -> handleDeposit(in);                  // TELLER, MANAGER
                case "6" -> handleWithdraw(in);                 // TELLER, MANAGER
                case "7" -> handleTransactionHistory(in);       // TELLER, MANAGER

                // 📊 Manager only
                case "8" -> handleExportTransactions(in);       // MANAGER

                // 🔐 Session control
                case "9" -> handleLogout(in);                   // Logout
                case "0" -> {                                  // Exit application
                    handleExit();
                    return;
                }

                default -> System.out.println("[!] Invalid choice. Please try again.");
            }

        }

    }

    /* ========================= Helpers ========================= */

    private static String readAndValidateNationalId(Scanner in) {
        System.out.print("Enter Customer National ID: ");
        String nationalId = in.nextLine().trim();
        try {
            NationalIdValidator.validateNationalId(nationalId);
            return nationalId;
        } catch (InvalidNationalIdException e) {
            System.out.println("[!] " + e.getMessage());
            return null;
        }
    }

    private static BigDecimal readBigDecimal(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = in.nextLine().trim();

            // optional escape
            if (input.equalsIgnoreCase("q")) {
                System.out.println("[!] Operation cancelled.");
                return null;
            }

            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid amount format. Please enter a valid number.");
            }
        }
    }


    private static Account chooseAccountFromCustomer(Scanner in, Customer customer) {

        var accounts = customer.getAccounts();

        if (accounts.isEmpty()) {
            System.out.println("[!] Customer has no accounts.");
            return null;
        }

        // if customer has only one account → auto select it
        if (accounts.size() == 1) {
            System.out.println(
                            accounts.getFirst().getAccountType().label()+ " Account, auto-selected."
            );
            return accounts.getFirst();
        }


        // multiple accounts → ask user to choose
        System.out.println("\nCustomer Accounts:");

        for (int i = 0; i < accounts.size(); i++) {

            System.out.printf(
                    "%d) %s | Account Number: %s%n",
                    i + 1,
                    accounts.get(i).getAccountType().label(),
                    NumberFormatter.mask(accounts.get(i).getAccountNumber(), 4)
            );
        }


        System.out.print("Choose account number: ");
        try {
            int choice = Integer.parseInt(in.nextLine());

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


    /* ========================= Menu ========================= */

    private static void printMenu() {

        System.out.println("----------------------------------------");
        System.out.println(" Finance Bank - Main Menu ");
        System.out.println("----------------------------------------");

        switch (currentEmployee.getRole()) {

            case CS -> {
                // Customer Service operations
                System.out.println("1. Create Customer");
                System.out.println("2. Add Account");
                System.out.println("3. Show Customers");

                System.out.println("----------------------------------------");
                System.out.println("9. Logout");
                System.out.println("0. Exit");
            }

            case TELLER -> {
                // Teller daily operations
                System.out.println("5. Deposit");
                System.out.println("6. Withdraw");
                System.out.println("7. Show Transaction History");

                System.out.println("----------------------------------------");
                System.out.println("9. Logout");
                System.out.println("0. Exit");
            }

            case MANAGER -> {
                // Manager full access
                System.out.println("1. Create Customer");
                System.out.println("2. Add Account");
                System.out.println("3. Show Customers");
                System.out.println("4. Show Accounts By National ID");

                System.out.println("----------------------------------------");

                System.out.println("5. Deposit");
                System.out.println("6. Withdraw");
                System.out.println("7. Show Transaction History");
                System.out.println("8. Export Transaction History to Excel (CSV)");

                System.out.println("----------------------------------------");
                System.out.println("9. Logout");
                System.out.println("0. Exit");
            }
        }

        System.out.println("----------------------------------------");
    }

    private static void handleCreateCustomer(Scanner in) {

        if (currentEmployee == null) {
            System.out.println("[!] Please login first");
            return;
        }

        System.out.print("\nEnter Customer Name: ");
        String name = in.nextLine().trim();

        String nationalId = readAndValidateNationalId(in);
        if (nationalId == null) return;

        try {
            Customer c =
                    bankService.createCustomer(
                            currentEmployee,
                            name,
                            nationalId
                    );

            System.out.println("\n===== Customer Created Successfully =====");
            System.out.printf("System ID : %s%n", c.getSystemId());
            System.out.printf("Customer Name : %s%n", c.getName());
            System.out.printf("National ID : %s%n", c.getNationalId());
            System.out.println("=========================================");

        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }

    // Note:
    // Customers are allowed to have multiple accounts of the same type.
    // This reflects real-world banking scenarios and keeps the system flexible.

    private static void handleAddAccount(Scanner in) {

        // Guard: ensure employee is logged in
        if (currentEmployee == null) {
            System.out.println("[!] Please login first");
            return;
        }

        System.out.print("\nEnter Account Type (1 for Savings, 2 for Current): ");
        String accountType = in.nextLine().trim();

        Customer customer = readAndValidateCustomer(in);
        if (customer == null) return;

        String accountNumber = IdGenerator.generateAccountNumber();
        System.out.println(
                "Generated Account Number : "
                        + NumberFormatter.mask(accountNumber, 4)
        );

        try {
            switch (accountType) {

                case "1" -> {
                    SavingsAccount sa =
                            new SavingsAccount(accountNumber, customer);

                    // Authorization happens inside BankService
                    bankService.openAccount(currentEmployee, sa);

                    System.out.println("✓ Savings Account Created");
                }

                case "2" -> {
                    BigDecimal overdraft =
                            readBigDecimal(in, "Enter Overdraft Limit: ");
                    if (overdraft == null) return;

                    CurrentAccount ca =
                            new CurrentAccount(accountNumber, customer, overdraft);

                    // Authorization happens inside BankService
                    bankService.openAccount(currentEmployee, ca);

                    System.out.println("✓ Current Account Created");
                }

                default -> System.out.println("[!] Invalid account type.");
            }

        } catch (Exception e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    private static void handleDeposit(Scanner in) {

        // Guard: ensure employee is logged in
        Account account = getValidatedAccount(in);
        if (account == null) return;

        BigDecimal amount = readBigDecimal(in, "Enter deposit amount: ");
        if (amount == null) return;


        try {
            // Business + Authorization + Transaction handled in service
            bankService.deposit(
                    currentEmployee,            // employee context
                    account.getAccountNumber(), // target account
                    amount
            );

            System.out.println("✓ Deposit successful");
            System.out.println("New Balance: " + account.getBalance());

        } catch (Exception e) {
            System.out.println("[!] " + e.getMessage());
        }
    }
//     Guard: ensure employee is logged in
    private static Account getValidatedAccount(Scanner in) {

        if (currentEmployee == null) {
            System.out.println("[!] Please login first");
            return null;
        }

        Customer customer = readAndValidateCustomer(in);
        if (customer == null) return null;

        return chooseAccountFromCustomer(in, customer);
    }

    private static void handleWithdraw(Scanner in) {

        // Guard: ensure employee is logged in

        Account account = getValidatedAccount(in);
        if (account == null) return;

        BigDecimal amount = readBigDecimal(in, "Enter withdrawal amount: ");
        if (amount == null) return;


        try {
            // Business + Authorization + Transaction handled in service
            bankService.withdraw(
                    currentEmployee,            // employee context
                    account.getAccountNumber(), // target account
                    amount
            );

            System.out.println("✓ Withdrawal successful");
            System.out.println("New Balance: " + account.getBalance());

        } catch (Exception e) {
            System.out.println("[!] " + e.getMessage());
        }
    }


    private static void handleTransactionHistory(Scanner in) {
        Customer customer = readAndValidateCustomer(in);
        if (customer == null) return;

        Account account = chooseAccountFromCustomer(in, customer);
        if (account == null) return;
        List<Transaction> transactions =
                bankService.getTransactionsByAccount(
                        account.getAccountNumber()
                );
        if (transactions.isEmpty()) {
            System.out.println("[!] No transactions found.");
            return;
        }

        System.out.println("\n===== Transaction History =====");
        System.out.printf("Account Type: %s%n", account.getAccountType().label());
        System.out.println("--------------------------------");

        transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .forEach(t -> System.out.printf(
                        "%s | Amount: %s | Balance After: %s | %s | %s%n",
                        t.getType(),
                        t.getAmount(),
                        t.getBalanceAfter(),
                        timeFormatter(t.getTimestamp()),
                        t.getTransactionId()
                ));

        System.out.println("================================");
    }
    private static void handleExportTransactions(Scanner in) {

        Customer customer = readAndValidateCustomer(in);
        if (customer == null) return;

        Account account = chooseAccountFromCustomer(in, customer);
        if (account == null) return;

        List<Transaction> transactions =
                bankService.getTransactionsByAccount(account.getAccountNumber());

        if (transactions.isEmpty()) {
            System.out.println("[!] No transactions to export.");
            return;
        }

        TransactionPrinter.exportNewTransactions(
                account.getAccountNumber(),
                transactions
        );
    }



    private static Customer readAndValidateCustomer(Scanner in) {
        String nationalId = readAndValidateNationalId(in);
        if (nationalId == null) return null;

        Customer customer = bankService.findCustomerByNationalId(nationalId);
        if (customer == null) {
            System.out.println("[!] Customer not found.");
            return null;
        }

        return customer;
    }

    private static void handleShowCustomers() {
        List<Customer> customers = bankService.getCustomers();
        if (customers.isEmpty()) {
            System.out.println("[!] No customers registered yet.");
            return;
        }

        System.out.println("\n========== Customers List ==========");
        for (Customer c : customers) {
            System.out.printf("Name: %s | National ID: %s | Accounts: %d%n",
                    c.getName(), c.getNationalId(), c.getAccounts().size());
        }
        System.out.println("====================================");
    }

    private static void handleShowAccountsByNationalId(Scanner in) {
    Customer customer = readAndValidateCustomer(in);
    if (customer == null) {
        return;
    }
        System.out.printf("\nCustomer: %s%n", customer.getName());
        List<Account> customerAccounts = customer.getAccounts();
        if (customerAccounts.isEmpty()) {
            System.out.println("[!] No accounts found for this customer.");
            return;
        }
        System.out.println("Accounts:");
        customerAccounts.forEach(a ->
                System.out.printf("- %s | %s%n",
                       NumberFormatter.mask( a.getAccountNumber(), 4),
                        a.getAccountType().label())
        );

    }

    private static void handleExit() {
        System.out.println("\nThank you for using Finance Bank. Exiting...");
    }
}

