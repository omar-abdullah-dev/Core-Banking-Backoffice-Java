package com.finance.bank.util;

import com.finance.bank.model.EmployeeRole;

public class RoleValidator {

    private RoleValidator() {}

    public static void validate(EmployeeRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Employee role cannot be null");
        } else if (role != EmployeeRole.TELLER && role != EmployeeRole.MANAGER && role != EmployeeRole.CS) {
            throw new IllegalArgumentException("User role " + role + " not allowed");
        }
    }
}
