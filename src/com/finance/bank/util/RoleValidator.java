package com.finance.bank.util;

import com.finance.bank.model.Role;

public class RoleValidator {

    private RoleValidator() {}

    public static void validate(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Employee role cannot be null");
        } else if (role != Role.TELLER && role != Role.MANAGER && role != Role.CS) {
            throw new IllegalArgumentException(STR."Invalid employee role: \{role}");
        }
    }
}
