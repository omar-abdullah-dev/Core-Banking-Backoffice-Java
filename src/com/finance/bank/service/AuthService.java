package com.finance.bank.service;

import com.finance.bank.model.Employee;
import com.finance.bank.model.Role;

import java.util.ArrayList;
import java.util.List;

public class AuthService {

    // In-memory employees (temporary)
    private static final List<Employee> EMPLOYEES = new ArrayList<>();

    static {
        EMPLOYEES.add(
                new Employee(
                        "Omar CS",
                        "omarPass!",
                        "30212121700915",
                        Role.CS
                )
        );
        EMPLOYEES.add(
                new Employee(
                        "Ahmed Manager",
                        "ahmedPass!",
                        "30111111700915",
                        Role.MANAGER
                )
        );

        EMPLOYEES.add(
                new Employee(
                        "Mohamed Teller",
                        "MohamedPass!",
                        "30111111700916",
                        Role.TELLER
                )
        );
    }

    /**
     * Authenticates employee using username & password
     */
    public Employee login(String username, String password) {
        for (Employee employee : EMPLOYEES) {
            if (employee.getUserName().equals(username)
                    && employee.matchesPassword(password)) {
                return employee;
            }
        }
        return null;
    }
}
