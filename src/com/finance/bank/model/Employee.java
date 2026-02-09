package com.finance.bank.model;

import com.finance.bank.util.PasswordValidator;
import com.finance.bank.util.RoleValidator;

public class Employee extends Person {
    private final Role role;
    private final String password;
    public Employee(String userName, String password, String nationalId, Role role) {
        super(userName, nationalId);
        this.password = password;
        this.role = role;
    }
    // Optional: full constructor (if loading from storage)
    public Employee(String systemId, String userName ,String nationalId,
                    String email,String phone,String password,Role role)
    {

        super(systemId, userName, nationalId, email, phone);
        RoleValidator.validate(role);
        PasswordValidator.validate(password);
        this.password = password;
        this.role = role;
    }

    public boolean matchesPassword(String inputPassword) {
        return password.equals(inputPassword);
    }

    public String getUserName() {
        return getName();
    }

    public String getRole() {
        return role.toString();
    }

}
