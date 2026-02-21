package com.finance.bank.presentation.controllers;

import com.finance.bank.model.*;
import com.finance.bank.presentation.util.AlertHelper;
import com.finance.bank.presentation.util.SessionManager;
import com.finance.bank.service.BankService;
import com.finance.bank.util.NumberFormatter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Deposit and Withdraw screens.
 * Mode is set by DashboardController.loadTransactionForm("DEPOSIT" | "WITHDRAW").
 *
 * 3-step flow:
 *   1. Find customer by National ID
 *   2. Select account from ComboBox
 *   3. Enter amount and confirm
 *
 * Calls BankService.deposit() or BankService.withdraw() — no direct repository access.
 * Employee is always read from SessionManager.
 */
public class TransactionFormController {

    @FXML private Label       pageHeaderLabel;
    @FXML private Label       formTitle;
    @FXML private TextField   nationalIdField;
    @FXML private HBox        customerFoundBox;
    @FXML private Label       foundCustomerName;
    @FXML private VBox        accountSelectionBox;
    @FXML private ComboBox<String> accountCombo;
    @FXML private HBox        balanceBox;
    @FXML private Label       currentBalanceLabel;
    @FXML private Label       accountTypeLabel;
    @FXML private VBox        amountBox;
    @FXML private TextField   amountField;
    @FXML private Label       feeLabel;
    @FXML private Button      actionButton;
    @FXML private VBox        alertContainer;

    // Receipt panel
    @FXML private VBox  receiptCard;
    @FXML private Label receiptTitle;
    @FXML private Label receiptTxId;
    @FXML private Label receiptAmount;
    @FXML private Label receiptBalance;
    @FXML private Label receiptEmployee;
    @FXML private Label receiptTimestamp;

    private final BankService bankService = BankService.getInstance();
    private Customer selectedCustomer;
    private Account  selectedAccount;
    private String   mode; // "DEPOSIT" or "WITHDRAW"

    // Holds the list of accounts for the selected customer (parallel to ComboBox items)
    private List<Account> customerAccounts = new ArrayList<>();

    /**
     * Called by DashboardController after loading this FXML.
     * @param employee The logged-in employee (also available via SessionManager)
     * @param mode     "DEPOSIT" or "WITHDRAW"
     */
    public void initializeForm(Employee employee, String mode) {
        this.mode = mode;
        setupUI();
    }

    private void setupUI() {
        boolean isDeposit = "DEPOSIT".equals(mode);

        pageHeaderLabel.setText(isDeposit ? "Deposit" : "Withdraw");
        formTitle.setText(isDeposit ? "Deposit Funds" : "Withdraw Funds");

        String buttonText = isDeposit ? "Confirm Deposit" : "Confirm Withdrawal";
        actionButton.setText(buttonText);

        String actionColor = isDeposit ? "#1A7A4A" : "#C0392B";

        actionButton.setStyle(
                "-fx-background-color: " + actionColor + "; "
                        + "-fx-text-fill: white; "
                        + "-fx-background-radius: 8; "
                        + "-fx-font-weight: bold; "
                        + "-fx-font-size: 13px; "
                        + "-fx-cursor: hand; "
                        + "-fx-pref-height: 40; "
                        + "-fx-pref-width: 150; "
                        + "-fx-padding: 0 5 0 5;"
        );
        // Live fee display for withdrawals
         amountField.textProperty().addListener((obs, old, text) -> updateFeeDisplay(text));
    }

    @FXML
    private void handleSearchCustomer() {
        clearAlerts();
        String nationalId = nationalIdField.getText().trim();

        if (nationalId.isEmpty()) {
            showError("Please enter a National ID.");
            return;
        }

        Customer customer = bankService.findCustomerByNationalId(nationalId);
        if (customer == null) {
            showError("No customer found with National ID: " + nationalId);
            hideAccountSection();
            selectedCustomer = null;
            customerFoundBox.setVisible(false);
            customerFoundBox.setManaged(false);
            return;
        }

        if (customer.getAccounts().isEmpty()) {
            showError("Customer \"" + customer.getName() + "\" has no accounts.");
            hideAccountSection();
            selectedCustomer = null;
            customerFoundBox.setVisible(false);
            customerFoundBox.setManaged(false);
            return;
        }

        selectedCustomer  = customer;
        customerAccounts  = new ArrayList<>(customer.getAccounts());

        foundCustomerName.setText(customer.getName()
                + "  \u2502  " + customerAccounts.size() + " account(s)");
        customerFoundBox.setVisible(true);
        customerFoundBox.setManaged(true);

        // Build ComboBox items: "Savings — XXXXXXXXXXXXABCD  (EGP 1,000.00)"
        List<String> labels = new ArrayList<>();
        for (Account acc : customerAccounts) {
            labels.add(acc.getAccountType().label()
                    + " \u2014 " + NumberFormatter.mask(acc.getAccountNumber(), 4)
                    + "  (EGP " + String.format("%,.2f", acc.getBalance().doubleValue()) + ")");
        }
        accountCombo.setItems(FXCollections.observableArrayList(labels));
        accountCombo.getSelectionModel().clearSelection();

        accountSelectionBox.setVisible(true);
        accountSelectionBox.setManaged(true);
        amountBox.setVisible(false);
        amountBox.setManaged(false);
        balanceBox.setVisible(false);
        balanceBox.setManaged(false);
    }

    @FXML
    private void handleAccountSelected() {
        int idx = accountCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= customerAccounts.size()) return;

        selectedAccount = customerAccounts.get(idx);

        currentBalanceLabel.setText(
                "EGP " + String.format("%,.2f", selectedAccount.getBalance().doubleValue()));
        accountTypeLabel.setText(selectedAccount.getAccountType().label());

        balanceBox.setVisible(true);
        balanceBox.setManaged(true);
        amountBox.setVisible(true);
        amountBox.setManaged(true);
        amountField.requestFocus();
    }

    @FXML
    private void handleTransaction() {
        clearAlerts();

        Employee employee = SessionManager.getEmployee();
        if (employee == null) {
            showError("Session expired. Please log in again.");
            return;
        }
        if (selectedAccount == null) {
            showError("Please select an account first.");
            return;
        }

        String amtText = amountField.getText().trim();
        if (amtText.isEmpty()) {
            showError("Please enter an amount.");
            amountField.requestFocus();
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amtText);
        } catch (NumberFormatException e) {
            showError("Invalid amount. Please enter a valid number (e.g. 500.00).");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            showError("Amount must be greater than zero.");
            return;
        }

        try {
            Transaction tx;
            if ("DEPOSIT".equals(mode)) {
                tx = bankService.deposit(employee, selectedAccount.getAccountNumber(), amount);
            } else {
                tx = bankService.withdraw(employee, selectedAccount.getAccountNumber(), amount);
            }

            showReceipt(tx);
            refreshBalanceDisplay();
            refreshAccountCombo();  // Update balance in combo label
            amountField.clear();
            feeLabel.setText("");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        nationalIdField.clear();
        amountField.clear();
        feeLabel.setText("");
        customerFoundBox.setVisible(false);
        customerFoundBox.setManaged(false);
        hideAccountSection();
        receiptCard.setVisible(false);
        receiptCard.setManaged(false);
        selectedCustomer = null;
        selectedAccount  = null;
        customerAccounts.clear();
        clearAlerts();
    }

    // ── Private Helpers ────────────────────────────────────────────

    private void showReceipt(Transaction tx) {
        receiptTitle.setText("\u2713  " + ("DEPOSIT".equals(mode) ? "Deposit" : "Withdrawal") + " Successful");
        receiptTxId.setText(tx.getTransactionId());
        receiptAmount.setText("EGP " + String.format("%,.2f", tx.getAmount().doubleValue()));
        receiptBalance.setText("EGP " + String.format("%,.2f", tx.getBalanceAfter().doubleValue()));
        receiptEmployee.setText(tx.getPerformedByEmployeeName()
                + " (" + tx.getPerformedByRole() + ")");
        receiptTimestamp.setText(NumberFormatter.timeFormatter(tx.getTimestamp()));
        receiptCard.setVisible(true);
        receiptCard.setManaged(true);
        showSuccess("Transaction completed successfully.");
    }

    private void refreshBalanceDisplay() {
        if (selectedAccount != null) {
            currentBalanceLabel.setText(
                    "EGP " + String.format("%,.2f", selectedAccount.getBalance().doubleValue()));
        }
    }

    private void refreshAccountCombo() {
        if (selectedCustomer == null) return;
        // Refresh labels to reflect updated balances
        List<String> labels = new ArrayList<>();
        for (Account acc : customerAccounts) {
            labels.add(acc.getAccountType().label()
                    + " \u2014 " + NumberFormatter.mask(acc.getAccountNumber(), 4)
                    + "  (EGP " + String.format("%,.2f", acc.getBalance().doubleValue()) + ")");
        }
        int currentIdx = accountCombo.getSelectionModel().getSelectedIndex();
        accountCombo.setItems(FXCollections.observableArrayList(labels));
        if (currentIdx >= 0) accountCombo.getSelectionModel().select(currentIdx);
    }

    private void updateFeeDisplay(String text) {
        if ("WITHDRAW".equals(mode) && text != null && !text.isEmpty()) {
            try {
                BigDecimal amt  = new BigDecimal(text);
                BigDecimal fee  = amt.multiply(new BigDecimal("0.01"))
                                     .setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = amt.add(fee);
                feeLabel.setText("Withdrawal fee (1%): EGP "
                        + String.format("%,.2f", fee.doubleValue())
                        + "  |  Total deducted: EGP "
                        + String.format("%,.2f", total.doubleValue()));
            } catch (NumberFormatException e) {
                feeLabel.setText("");
            }
        } else {
            feeLabel.setText("DEPOSIT".equals(mode) ? "No fee for deposits." : "");
        }
    }

    private void hideAccountSection() {
        accountSelectionBox.setVisible(false);
        accountSelectionBox.setManaged(false);
        amountBox.setVisible(false);
        amountBox.setManaged(false);
        balanceBox.setVisible(false);
        balanceBox.setManaged(false);
    }

    private void showError(String msg) {
        AlertHelper.showAlert(alertContainer, msg, AlertHelper.AlertType.ERROR);
    }

    private void showSuccess(String msg) {
        AlertHelper.showAlert(alertContainer, msg, AlertHelper.AlertType.SUCCESS);
    }

    private void clearAlerts() {
        alertContainer.getChildren().clear();
    }
}
