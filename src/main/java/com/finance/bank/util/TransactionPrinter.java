package com.finance.bank.util;

import com.finance.bank.model.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.finance.bank.util.NumberFormatter.timeFormatter;

public final class TransactionPrinter {

    // prevent instantiation
    private TransactionPrinter() {}

    // Track last export time per account
    private static final Map<String, Instant> lastExportedTimeMap = new HashMap<>();

    public static void exportNewTransactions(String accountNumber, List<Transaction> transactions) {

        // Ensure exports directory exists
        File directory = new File("exports");

        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create exports directory");
        }

        String fileName = "exports/transactions_" + accountNumber + ".csv";
        File file = new File(fileName);
        boolean fileExists = file.exists();

        Instant lastExportedTime = lastExportedTimeMap.get(accountNumber);

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
                writer.println("AccountNumber,Type,Amount,BalanceAfter,PerformedBy,Role,Timestamp,TransactionId");
            }

            for (Transaction t : newTransactions) {
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

            // safer than getLast()
            Instant latestTimestamp = newTransactions.get(newTransactions.size() - 1).getTimestamp();
            lastExportedTimeMap.put(accountNumber, latestTimestamp);

            System.out.println("✓ New transactions exported successfully");
            System.out.println("File: " + file.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("[!] Export failed: " + e.getMessage());
        }
    }
}