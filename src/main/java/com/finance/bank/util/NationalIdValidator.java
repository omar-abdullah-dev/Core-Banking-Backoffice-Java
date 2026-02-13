package com.finance.bank.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Set;

import com.finance.bank.exception.InvalidNationalIdException;

public final class NationalIdValidator {

    private static final Set<String> GOVERNORATE_CODES = Set.of(
            "01","02","03","04","11","12","13","14","15","16",
            "17","18","19","21","22","23","24","25","26","27",
            "28","29","31","32","33","34","35"
    );

    private NationalIdValidator() {}

    public static void validateNationalId(String rawId) {
        if (rawId == null) {
            throw new InvalidNationalIdException("National ID cannot be null");
        }

        // remove BOM if copy-pasted from some editors and trim whitespace
        String id = rawId.replace("\uFEFF", "").trim();

        if (id.isEmpty()) {
            throw new InvalidNationalIdException("National ID cannot be empty or blank");
        }

        // must be 14 digits and start with 2 or 3
        if (!id.matches("^[23]\\d{13}$")) {
            throw new InvalidNationalIdException("National ID must be exactly 14 digits and start with 2 or 3");
        }

        // year/month/day validation
        int century = (id.charAt(0) == '2') ? 1900 : 2000;
        int yearPart = Integer.parseInt(id.substring(1, 3));
        int year = century + yearPart;
        int month = Integer.parseInt(id.substring(3, 5));
        int day = Integer.parseInt(id.substring(5, 7));

        try {
            LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new InvalidNationalIdException("Invalid birth date in National ID (year=" + year + ", month=" + month + ", day=" + day + ")", e);
        }

        // governorate code (positions 7-8)
        String govCode = id.substring(7, 9);
        if (!GOVERNORATE_CODES.contains(govCode)) {
            throw new InvalidNationalIdException("Invalid governorate code in National ID: " + govCode);
        }

        // (optional) add checksum validation here if you have the algorithm
    }
}
