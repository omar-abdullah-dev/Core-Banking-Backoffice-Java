package com.finance.bank.integration;

import com.finance.bank.exception.AuthenticationException;
import com.finance.bank.model.Employee;
import com.finance.bank.model.EmployeeRole;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for employee authentication.
 *
 * AuthenticationService reads employees from the DB (seeded by schema INSERT statements).
 * These tests verify the real login flow against the actual PostgreSQL employees table.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest extends IntegrationTestBase {

    @Test
    @Order(1)
    @DisplayName("Manager login succeeds with correct credentials")
    void login_manager_validCredentials_succeeds() throws Exception {
        Employee emp = authService.login("manager", "manager123");

        assertNotNull(emp);
        assertEquals("manager",          emp.getUserName());
        assertEquals(EmployeeRole.MANAGER, emp.getRole());
        assertNotNull(emp.getSystemId());
    }

    @Test
    @Order(2)
    @DisplayName("Teller login succeeds with correct credentials")
    void login_teller_validCredentials_succeeds() throws Exception {
        Employee emp = authService.login("teller", "teller123");

        assertNotNull(emp);
        assertEquals("teller",           emp.getUserName());
        assertEquals(EmployeeRole.TELLER, emp.getRole());
    }

    @Test
    @Order(3)
    @DisplayName("CS login succeeds with correct credentials")
    void login_cs_validCredentials_succeeds() throws Exception {
        Employee emp = authService.login("cs", "cs123456");

        assertNotNull(emp);
        assertEquals("cs",           emp.getUserName());
        assertEquals(EmployeeRole.CS, emp.getRole());
    }

    @Test
    @Order(4)
    @DisplayName("Login with wrong password throws AuthenticationException")
    void login_wrongPassword_throwsAuthenticationException() {
        assertThrows(AuthenticationException.class,
                () -> authService.login("manager", "wrongpassword"));
    }

    @Test
    @Order(5)
    @DisplayName("Login with unknown username throws AuthenticationException")
    void login_unknownUsername_throwsAuthenticationException() {
        assertThrows(AuthenticationException.class,
                () -> authService.login("nobody", "password123"));
    }

    @Test
    @Order(6)
    @DisplayName("Login with null username throws AuthenticationException")
    void login_nullUsername_throwsAuthenticationException() {
        assertThrows(AuthenticationException.class,
                () -> authService.login(null, "password123"));
    }

    @Test
    @Order(7)
    @DisplayName("Login with null password throws AuthenticationException")
    void login_nullPassword_throwsAuthenticationException() {
        assertThrows(AuthenticationException.class,
                () -> authService.login("manager", null));
    }

    @Test
    @Order(8)
    @DisplayName("Production employee 'ahmed' (MANAGER) can login")
    void login_productionManager_succeeds() throws Exception {
        Employee emp = authService.login("ahmed", "ahmedPass!");

        assertNotNull(emp);
        assertEquals(EmployeeRole.MANAGER, emp.getRole());
    }

    @Test
    @Order(9)
    @DisplayName("Production employee 'omar' (CS) can login")
    void login_productionCS_succeeds() throws Exception {
        Employee emp = authService.login("omar", "omarPass!");

        assertNotNull(emp);
        assertEquals(EmployeeRole.CS, emp.getRole());
    }
}
