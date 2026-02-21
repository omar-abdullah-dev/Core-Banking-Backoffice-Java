package com.finance.bank.presentation.util;

import com.finance.bank.model.Employee;

/**
 * Centralized session state holder.
 * Stores the currently authenticated employee so all controllers
 * can access it without passing it through constructor chains.
 *
 * All JavaFX controllers call SessionManager.getEmployee() instead of
 * receiving the employee via constructor injection.
 */
public final class SessionManager {

    private static Employee currentEmployee;

    private SessionManager() {}

    /**
     * Set the currently authenticated employee after successful login.
     * Called by LoginController after AuthenticationService.login() succeeds.
     */
    public static void setEmployee(Employee employee) {
        currentEmployee = employee;
    }

    /**
     * Get the currently authenticated employee.
     * Returns null if no user is logged in.
     */
    public static Employee getEmployee() {
        return currentEmployee;
    }

    /**
     * Clear the session on logout.
     */
    public static void clearSession() {
        currentEmployee = null;
    }

    /**
     * Check whether an employee is currently logged in.
     */
    public static boolean isLoggedIn() {
        return currentEmployee != null;
    }
}
