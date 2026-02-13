package com.finance.bank.service;

import com.finance.bank.exception.AuthenticationException;
import com.finance.bank.model.Employee;
import com.finance.bank.model.Role;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationService {

    // In-memory employees (temporary)
    private static final List<Employee> EMPLOYEES = new ArrayList<>();

    static {
        EMPLOYEES.add(
                new Employee(
                        "omar",     // username
                        "omarPass!",         // password
                        "30212121700915",    // nationalId
                        Role.CS
                )
        );

        EMPLOYEES.add(
                new Employee(
                        "ahmed",
                        "ahmedPass!",
                        "30111111700915",
                        Role.MANAGER
                )
        );

        EMPLOYEES.add(
                new Employee(
                        "mohamed",
                        "mohamedPass!",
                        "30111111700916",
                        Role.TELLER
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
