package com.finance.bank.service;

import com.finance.bank.exception.AuthenticationException;
import com.finance.bank.model.Employee;
import com.finance.bank.model.EmployeeRole;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationService {

    // In-memory employees
    private static final List<Employee> EMPLOYEES = new ArrayList<>();

    static {
        // ============================================
        // PRODUCTION EMPLOYEES
        // ============================================
        EMPLOYEES.add(
                new Employee(
                        "omar",              // username
                        "omarPass!",         // password
                        "30212121700915",    // nationalId
                        EmployeeRole.CS
                )
        );

        EMPLOYEES.add(
                new Employee(
                        "ahmed",             // username
                        "ahmedPass!",        // password
                        "30111111700915",    // nationalId
                        EmployeeRole.MANAGER
                )
        );

        EMPLOYEES.add(
                new Employee(
                        "mohamed",           // username
                        "mohamedPass!",      // password
                        "30111111700916",    // nationalId
                        EmployeeRole.TELLER
                )
        );

        // ============================================
        // TEST EMPLOYEES (for JUnit tests)
        // Required by BankingSystemTest.java
        // ============================================
        EMPLOYEES.add(
                new Employee(
                        "manager",           // username
                        "manager123",        // password (11 chars) ✅
                        "29505051234567",    // nationalId
                        EmployeeRole.MANAGER
                )
        );

        EMPLOYEES.add(
                new Employee(
                        "teller",            // username
                        "teller123",         // password (10 chars) ✅
                        "29505051234568",    // nationalId
                        EmployeeRole.TELLER
                )
        );

        EMPLOYEES.add(
                new Employee(
                        "cs",                // username
                        "cs123456",          // password (8 chars) ✅ FIXED: was "cs123"
                        "29505051234569",    // nationalId
                        EmployeeRole.CS
                )
        );
    }

    /**
     * Authenticate employee using username & password
     */
    public Employee login(String username, String password) {

        if (username == null || password == null) {
            throw new AuthenticationException("Username and password are required");
        }

        username = username.trim();

        for (Employee employee : EMPLOYEES) {
            if (employee.getUserName().equals(username)
                    && employee.matchesPassword(password)) {
                return employee;
            }
        }

        throw new AuthenticationException("Invalid username or password");
    }
}