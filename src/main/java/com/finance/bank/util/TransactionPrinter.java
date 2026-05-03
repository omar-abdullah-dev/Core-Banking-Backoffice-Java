package com.finance.bank.util;

import com.finance.bank.model.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.finance.bank.util.NumberFormatter.timeFormatter;

public final class TransactionPrinter {

    // prevent instantiation
    private TransactionPrinter() {}

    // Track last export time per export key (account, nationalId, or "all")
    private static final Map<String, Instant> lastExportedTimeMap = new HashMap<>();

    private static final String EXPORTS_DIR = "exports";
    private static final String CSV_HEADER = "AccountNumber,Type,Amount,BalanceAfter,PerformedBy,Role,Timestamp,TransactionId";

    /**
     * Export transactions for a specific account
     */
    public static void exportByAccount(String accountNumber, List<Transaction> transactions) {
        String fileName = "transactions_account_" + accountNumber + ".csv";
        String exportKey = "account_" + accountNumber;
        exportTransactions(fileName, exportKey, transactions);
    }

    /**
     * Export all transactions for a customer (by National ID)
     */
    public static void exportByCustomer(String nationalId, List<Transaction> transactions) {
        String fileName = "transactions_customer_" + nationalId + ".csv";
        String exportKey = "customer_" + nationalId;
        exportTransactions(fileName, exportKey, transactions);
    }

    /**
     * Export all transactions (audit log)
     * Creates a timestamped file to avoid overwriting previous exports
     */
    public static void exportAllTransactions(String employeeName, List<Transaction> transactions) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "transactions_all_" + timestamp + "_by_" + sanitizeFileName(employeeName) + ".csv";
        // For "all" exports, we always create a new file (no incremental export)
        exportTransactionsFullFile(fileName, transactions);
    }

    /**
     * Core export logic with incremental export support (appends only new transactions)
     */
    private static void exportTransactions(String fileName, String exportKey, List<Transaction> transactions) {
        ensureExportsDirectory();

        File file = new File(EXPORTS_DIR, fileName);
        boolean fileExists = file.exists();

        Instant lastExportedTime = lastExportedTimeMap.get(exportKey);

        List<Transaction> newTransactions = transactions.stream()
                .filter(t -> lastExportedTime == null || t.getTimestamp().isAfter(lastExportedTime))
                .toList();

        if (newTransactions.isEmpty()) {
            System.out.println("[!] No new transactions to export.");
            return;
        }

        try (FileWriter fw = new FileWriter(file, true);
             PrintWriter writer = new PrintWriter(fw)) {

            // Write header only once
            if (!fileExists) {
                writer.println(CSV_HEADER);
            }

            writeTransactions(writer, newTransactions);

            // Update last exported time
            Instant latestTimestamp = newTransactions.get(newTransactions.size() - 1).getTimestamp();
            lastExportedTimeMap.put(exportKey, latestTimestamp);

            System.out.println("✓ Transactions exported successfully");
            System.out.println("File: " + file.getAbsolutePath());

        } catch (Exception e) {
            throw new RuntimeException("Export failed: " + e.getMessage(), e);
        }
    }

    /**
     * Export all transactions to a new file (no incremental - full export)
     */
    private static void exportTransactionsFullFile(String fileName, List<Transaction> transactions) {
        ensureExportsDirectory();

        File file = new File(EXPORTS_DIR, fileName);

        try (FileWriter fw = new FileWriter(file, false);
             PrintWriter writer = new PrintWriter(fw)) {

            writer.println(CSV_HEADER);
            writeTransactions(writer, transactions);

            System.out.println("✓ All transactions exported successfully");
            System.out.println("File: " + file.getAbsolutePath());

        } catch (Exception e) {
            throw new RuntimeException("Export failed: " + e.getMessage(), e);
        }
    }

    private static void writeTransactions(PrintWriter writer, List<Transaction> transactions) {
        for (Transaction t : transactions) {
            writer.printf(
                    "%s,%s,%s,%s,%s,%s,%s,%s%n",
                    t.getAccountNumber(),
                    t.getType(),
                    t.getAmount(),
                    t.getBalanceAfter(),
                    t.getPerformedByEmployeeName(),
                    t.getPerformedByRole(),
                    timeFormatter(t.getTimestamp()),
                    t.getTransactionId()
            );
        }
    }

    private static void ensureExportsDirectory() {
        File directory = new File(EXPORTS_DIR);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create exports directory");
        }
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }

    public static void exportNewTransactions(String accountNumber, List<Transaction> transactions) {
        exportByAccount(accountNumber, transactions);
    }
}