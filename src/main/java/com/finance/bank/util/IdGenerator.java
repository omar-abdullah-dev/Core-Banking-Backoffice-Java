package com.finance.bank.util;

import java.util.UUID;

public class IdGenerator {
    private static final String CUSTOMER_ID_PREFIX = "FIN-";
    private static final String BANK_CODE = "1001";
    private static final String BRANCH_CODE = "0001";
    private static long accountSequence = 1;

    public static String getBankCode() {
        return BANK_CODE;
    }
    public static String getBranchCode() {
        return BRANCH_CODE;
    }

    public static String generateCustomerId() {
        return "FIN-" + UUID.randomUUID();
    }


    public static String generateAccountNumber() {
        String sequenceStr = String.format("%08d", accountSequence++);
        return BANK_CODE + BRANCH_CODE + sequenceStr;
    }

    public static String generateTransactionId() {
        return "TXN-" + UUID.randomUUID();
    }


}

