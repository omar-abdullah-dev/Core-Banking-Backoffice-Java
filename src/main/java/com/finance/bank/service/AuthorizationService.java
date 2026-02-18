package com.finance.bank.service;

import com.finance.bank.exception.UnauthorizedException;
import com.finance.bank.model.Employee;
import com.finance.bank.model.EmployeeRole;

/**
   This service will implement role-based access control (RBAC) to manage permissions effectively.
   It will also handle authentication processes, such as verifying user credentials.
   Additionally, the AuthorizationService will log all access attempts and actions for auditing purposes,
   ensuring compliance with security standards and regulations.

 * Centralized authorization service.
 * Responsible for enforcing role-based access rules
 * across the banking back-office system.
 */
public class AuthorizationService {

    /**
     * Ensures that an employee is authenticated (logged in)
     * before performing any protected operation.
     *
     * @param employee the currently authenticated employee
     * @throws UnauthorizedException if employee is null
     */
    public void ensureLoggedIn(Employee employee) {
        if (employee == null) {
            throw new UnauthorizedException("Employee must be logged in");
        }
    }

    /**
     * Ensures that the employee is allowed to create customers.
     * Allowed roles: CS, MANAGER
     *
     * @param employee the performing employee
     * @throws UnauthorizedException if role is not permitted
     */
    public void ensureCanCreateCustomer(Employee employee) {
        ensureLoggedIn(employee);
//      if CS or MANAGER, allow. If TELLER, deny.
//      --> faster checking for TELLER first since they are more likely to be denied
        if (employee.getRole() == EmployeeRole.TELLER) {
            throw new UnauthorizedException(
                    "Teller is not allowed to create customers"
            );
        }
    }

    /**
     * Ensures that the employee is allowed to add bank accounts.
     * Allowed roles: CS, MANAGER
     *
     * @param employee the performing employee
     * @throws UnauthorizedException if role is not permitted
     */
    public void ensureCanAddAccount(Employee employee) {
        ensureLoggedIn(employee);

        if (employee.getRole() == EmployeeRole.TELLER) {
            throw new UnauthorizedException(
                    "Teller is not allowed to add accounts"
            );
        }
    }

    /**
     * Ensures that the employee is allowed to perform deposit operations.
     * Allowed roles: CS, TELLER, MANAGER
     *
     * @param employee the performing employee
     * @throws UnauthorizedException if employee is not logged in
     */
    public void ensureCanDeposit(Employee employee) {
        ensureLoggedIn(employee);

        if (employee.getRole() != EmployeeRole.CS
                && employee.getRole() != EmployeeRole.TELLER
                && employee.getRole() != EmployeeRole.MANAGER) {

            throw new UnauthorizedException(
                    "Role " + employee.getRole() + " is not allowed to deposit"
            );
        }
    }
    /**
     * Ensures that the employee is allowed to perform withdrawal operations.
     * Allowed roles: CS, TELLER, MANAGER
     *
     * @param employee the performing employee
     * @throws UnauthorizedException if employee is not logged in
     */
    public void ensureCanWithdraw(Employee employee) {
        ensureLoggedIn(employee);

        if (employee.getRole() != EmployeeRole.CS
                && employee.getRole() != EmployeeRole.TELLER
                && employee.getRole() != EmployeeRole.MANAGER) {

            throw new UnauthorizedException(
                    "Role " + employee.getRole() + " is not allowed to withdraw"
            );
        }
    }
}
