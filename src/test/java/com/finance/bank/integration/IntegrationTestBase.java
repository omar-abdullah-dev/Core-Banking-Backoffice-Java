package com.finance.bank.integration;

import com.finance.bank.config.DatabaseConfig;
import com.finance.bank.model.Employee;
import com.finance.bank.service.AuthenticationService;
import com.finance.bank.service.BankService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class IntegrationTestBase {

    protected BankService bankService;
    protected AuthenticationService authService;

    protected Employee manager;
    protected Employee teller;
    protected Employee cs;

    // ── Account number helper ──────────────────────────────────────────────────
    protected static String acc(int suffix) {
        return String.format("10010001%08d", suffix);
    }

    // ── National ID helper ────────────────────────────────────────────────────
    // Format must be exactly 14 digits, starting with 2 or 3
    // 2  90  01  01  01  XXXXX
    // ^  ^^  ^^  ^^  ^^  ^^^^^
    // |  yr  mo  dy  gov  seq
    // gov "01" = Cairo (valid governorate code)
    // Example: nationalId(1)  → "29001010100001"  ✅ 14 digits
    //          nationalId(99) → "29001010100099"  ✅ 14 digits
    protected static String nationalId(int index) {
        return String.format("290010101%05d", index);
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @BeforeAll
    static void initDatabase() {
        // src/test/resources/database.properties overrides production config automatically
    }

    @BeforeEach
    void setUp() throws Exception {
        bankService = BankService.getInstance();
        authService = new AuthenticationService();

        clearDataTables();

        manager = authService.login("manager", "manager123");
        teller  = authService.login("teller",  "teller123");
        cs      = authService.login("cs",       "cs123456");
    }

    protected void clearDataTables() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st   = conn.createStatement()) {
            st.executeUpdate("DELETE FROM transactions");
            st.executeUpdate("DELETE FROM accounts");
            st.executeUpdate("DELETE FROM customers");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear test data: " + e.getMessage(), e);
        }
    }
}