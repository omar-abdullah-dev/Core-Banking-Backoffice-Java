package com.finance.bank.util;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class NumberFormatter {
    //  disable instantiation of utility class or objects
    private NumberFormatter() {
        // prevent instantiation
    }
    private static final DateTimeFormatter TRANSACTION_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy - hh:mm a")
                    .withZone(ZoneId.systemDefault());
//    you can use this pattern "yyyy-MM-dd HH:mm:ss" if you want to include seconds
//    you can use this pattern "dd MMM yyyy, hh:mm a" for 12-hour format with AM/PM
//    you can use this pattern "EEEE, dd MMMM yyyy HH:mm" for full day and month names
//    you can use this pattern "dd MMM yyyy, HH:mm" for 24-hour format without seconds

    public static String amountFormatter(double amount) {
        DecimalFormat df = new DecimalFormat("0.00");

        return df.format(amount);
    }
//    transform Instant to formatted string date time
    public static String timeFormatter(Instant dateTime) {
        if (dateTime == null) return "N/A";
        return TRANSACTION_FORMATTER.format(dateTime);
    }
    /**
     * Masks an account number, leaving only the last N characters visible.
     * Example: 1234567890123456 -> XXXXXXXXXXXXX456
     */
    public static String mask(String accountNumber, int visibleChars) {

        if (accountNumber == null) {
            return "";
        }

        if (accountNumber.length() <= visibleChars) {
            return accountNumber;
        }

        return "X".repeat(accountNumber.length() - visibleChars)
                + accountNumber.substring(accountNumber.length() - visibleChars);
    }
}
