package com.finance.bank.util;

import com.finance.bank.model.Account;
import com.finance.bank.model.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;

import static com.finance.bank.util.NumberFormatter.timeFormatter;

public final class TransactionPrinter {
    //  disable instantiation of utility class or objects
    private TransactionPrinter() {
        // prevent instantiation
    }

    private static Instant lastExportedTime = null;

    public static void exportNewTransactions(Account account) {

        String fileName =
                "exports/transactions_" + account.getAccountNumber() + ".csv";

        File file = new File(fileName);
        boolean fileExists = file.exists();

        List<Transaction> newTransactions =
                account.getTransactions().stream()
                        .filter(t ->
                                lastExportedTime == null ||
                                        t.getTimestamp().isAfter(lastExportedTime)
                        )
                        .toList();

        if (newTransactions.isEmpty()) {
            System.out.println("[!] No new transactions to export.");
            return;
        }

        try (
                FileWriter fw = new FileWriter(file, true);
                PrintWriter writer = new PrintWriter(fw)
        ) {

            if (!fileExists) {
                writer.println(
                        "Type,Amount,Balance After,Timestamp,Transaction ID"
                );
            }

            for (Transaction t : newTransactions) {
                writer.printf(
                        "%s,%s,%s,%s,%s%n",
                        t.getType(),
                        t.getAmount(),
                        t.getBalanceAfter(),
                        timeFormatter(t.getTimestamp()),
                        t.getTransactionId()
                );
            }

            lastExportedTime =
                    newTransactions.getLast().getTimestamp();

            System.out.println("✓ New transactions exported successfully");
            System.out.println("File: " + file.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("[!] Export failed: " + e.getMessage());
        }
    }
}



