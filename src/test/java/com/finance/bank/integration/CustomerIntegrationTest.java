package com.finance.bank.integration;

import com.finance.bank.exception.DuplicateNationalIdException;
import com.finance.bank.exception.InvalidNationalIdException;
import com.finance.bank.exception.UnauthorizedException;
import com.finance.bank.model.Customer;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for customer management.
 *
 * Tests go through BankService (service layer) → CustomerRepository → PostgreSQL.
 * Every test starts with a clean database (no customer rows).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Customer Integration Tests")
class CustomerIntegrationTest extends IntegrationTestBase {

    // ─── CREATE ────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Manager can create a customer with a valid national ID")
    void createCustomer_validData_persistedToDb() throws Exception {
        Customer customer = bankService.createCustomer(manager, "Ahmed Hassan", nationalId(1));

        assertNotNull(customer);
        assertNotNull(customer.getSystemId(), "system ID must be generated");
        assertEquals("Ahmed Hassan", customer.getName());
        assertEquals(nationalId(1), customer.getNationalId());

        // Verify it actually landed in the DB
        Customer fromDb = bankService.findCustomerByNationalId(nationalId(1));
        assertNotNull(fromDb, "customer must be retrievable from DB");
        assertEquals(customer.getSystemId(), fromDb.getSystemId());
    }

    @Test
    @Order(2)
    @DisplayName("CS can create a customer")
    void createCustomer_byCS_succeeds() throws Exception {
        Customer customer = bankService.createCustomer(cs, "Mona Samir", nationalId(2));
        assertNotNull(customer);
        assertEquals("Mona Samir", customer.getName());
    }

    @Test
    @Order(3)
    @DisplayName("Teller cannot create a customer — UnauthorizedException")
    void createCustomer_byTeller_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class,
                () -> bankService.createCustomer(teller, "Blocked User", nationalId(3)));
    }

    // ─── VALIDATION ────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("Rejects a national ID that is too short")
    void createCustomer_shortNationalId_throwsInvalid() {
        assertThrows(InvalidNationalIdException.class,
                () -> bankService.createCustomer(manager, "Bad ID", "123"));
    }

    @Test
    @Order(5)
    @DisplayName("Rejects a national ID with letters")
    void createCustomer_alphaNumericNationalId_throwsInvalid() {
        assertThrows(InvalidNationalIdException.class,
                () -> bankService.createCustomer(manager, "Bad ID", "ABCDEFG1234567"));
    }

    @Test
    @Order(6)
    @DisplayName("Rejects an invalid governorate code in national ID")
    void createCustomer_invalidGovCode_throwsInvalid() {
        // Position 7-8 = "99" is not a valid governorate
        assertThrows(InvalidNationalIdException.class,
                () -> bankService.createCustomer(manager, "Bad Gov", "29001099123456"));
    }

    // ─── DUPLICATE ─────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("Duplicate national ID throws DuplicateNationalIdException")
    void createCustomer_duplicateNationalId_throwsDuplicate() throws Exception {
        bankService.createCustomer(manager, "First Person", nationalId(7));

        assertThrows(DuplicateNationalIdException.class,
                () -> bankService.createCustomer(manager, "Second Person", nationalId(7)));
    }

    // ─── FIND ──────────────────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("findCustomerByNationalId returns correct customer")
    void findCustomer_existingNationalId_returnsCustomer() throws Exception {
        bankService.createCustomer(manager, "Sara Ali", nationalId(8));

        Customer found = bankService.findCustomerByNationalId(nationalId(8));

        assertNotNull(found);
        assertEquals("Sara Ali", found.getName());
        assertEquals(nationalId(8), found.getNationalId());
    }

    @Test
    @Order(9)
    @DisplayName("findCustomerByNationalId returns null for unknown ID")
    void findCustomer_nonExistentNationalId_returnsNull() {
        Customer result = bankService.findCustomerByNationalId(nationalId(999));
        assertNull(result);
    }

    // ─── LIST ──────────────────────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("getCustomers returns all persisted customers")
    void getCustomers_multipleCreated_returnsAll() throws Exception {
        bankService.createCustomer(manager, "Customer A", nationalId(10));
        bankService.createCustomer(manager, "Customer B", nationalId(11));
        bankService.createCustomer(cs,      "Customer C", nationalId(12));

        List<Customer> all = bankService.getCustomers();

        assertEquals(3, all.size());
        assertTrue(all.stream().anyMatch(c -> c.getName().equals("Customer A")));
        assertTrue(all.stream().anyMatch(c -> c.getName().equals("Customer B")));
        assertTrue(all.stream().anyMatch(c -> c.getName().equals("Customer C")));
    }

    @Test
    @Order(11)
    @DisplayName("getCustomers returns empty list when no customers exist")
    void getCustomers_emptyDb_returnsEmptyList() {
        List<Customer> all = bankService.getCustomers();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }
}
