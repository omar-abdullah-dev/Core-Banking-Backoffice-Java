package com.finance.bank.presentation.controllers;

import com.finance.bank.model.Customer;
import com.finance.bank.model.Employee;
import com.finance.bank.presentation.util.AlertHelper;
import com.finance.bank.presentation.util.SessionManager;
import com.finance.bank.service.BankService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Controller for the Create Customer screen.
 * Calls BankService.createCustomer() only.
 * Employee is read from SessionManager.
 */
public class CustomerFormController implements EmployeeAware {

    @FXML private TextField nameField;
    @FXML private TextField nationalIdField;
    @FXML private Button    createButton;
    @FXML private VBox      alertContainer;
    @FXML private VBox      resultCard;
    @FXML private Label     resultSystemId;
    @FXML private Label     resultName;
    @FXML private Label     resultNationalId;

    private final BankService bankService = BankService.getInstance();

    @Override
    public void setEmployee(Employee employee) {
        // Employee is also available via SessionManager; this satisfies the interface.
    }

    @FXML
    private void handleCreateCustomer() {
        clearAlerts();

        Employee employee = SessionManager.getEmployee();
        if (employee == null) {
            showError("Session expired. Please log in again.");
            return;
        }

        String name       = nameField.getText().trim();
        String nationalId = nationalIdField.getText().trim();

        if (name.isEmpty()) {
            showError("Customer name is required.");
            nameField.requestFocus();
            return;
        }
        if (nationalId.isEmpty()) {
            showError("National ID is required.");
            nationalIdField.requestFocus();
            return;
        }

        try {
            Customer customer = bankService.createCustomer(employee, name, nationalId);
            showSuccess("Customer \"" + customer.getName() + "\" created successfully!");
            displayResult(customer);
            clearForm();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        clearAlerts();
        resultCard.setVisible(false);
        resultCard.setManaged(false);
    }

    private void displayResult(Customer customer) {
        resultSystemId.setText(customer.getSystemId());
        resultName.setText(customer.getName());
        resultNationalId.setText(customer.getNationalId());
        resultCard.setVisible(true);
        resultCard.setManaged(true);
    }

    private void clearForm() {
        nameField.clear();
        nationalIdField.clear();
        nameField.requestFocus();
    }

    private void showError(String message) {
        AlertHelper.showAlert(alertContainer, message, AlertHelper.AlertType.ERROR);
    }

    private void showSuccess(String message) {
        AlertHelper.showAlert(alertContainer, message, AlertHelper.AlertType.SUCCESS);
    }

    private void clearAlerts() {
        alertContainer.getChildren().clear();
    }
}
