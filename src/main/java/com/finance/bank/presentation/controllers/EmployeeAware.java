package com.finance.bank.presentation.controllers;

import com.finance.bank.model.Employee;

/**
 * Marker interface for controllers that need access to the current employee.
 * DashboardController passes the employee to sub-controllers via this interface.
 */
public interface EmployeeAware {
    void setEmployee(Employee employee);
}
