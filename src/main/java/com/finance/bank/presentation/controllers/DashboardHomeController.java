package com.finance.bank.presentation.controllers;

import com.finance.bank.model.*;
import com.finance.bank.presentation.util.SessionManager;
import com.finance.bank.service.BankService;
import com.finance.bank.util.NumberFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardHomeController {

    @FXML private Label welcomeLabel;
    @FXML private Label welcomeSub;
    @FXML private Label dateTimeLabel;
    @FXML private Label roleDisplayLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalAccountsLabel;
    @FXML private Label totalTransactionsLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private VBox quickActionsBox;
    @FXML private HBox permissionsBox;
    @FXML private Label recentTxCount;

    @FXML private TableView<Transaction> recentTransactionTable;
    @FXML private TableColumn<Transaction, String> recColType;
    @FXML private TableColumn<Transaction, String> recColAmount;
    @FXML private TableColumn<Transaction, String> recColAccount;
    @FXML private TableColumn<Transaction, String> recColEmployee;
    @FXML private TableColumn<Transaction, String> recColTime;

    private final BankService bankService = BankService.getInstance();
    private DashboardController dashboardController;

    /**
     * Called by DashboardController after the FXML is loaded.
     * Reads the employee from SessionManager — no need to pass it as parameter.
     */
    public void initializeDashboard(DashboardController dashController) {
        this.dashboardController = dashController;
        Employee employee = SessionManager.getEmployee();
        if (employee == null) return;

        setupWelcome(employee);
        loadStats();
        setupQuickActions(employee);
        setupPermissionBadges(employee);
        setupRecentTransactionTable();
        loadRecentTransactions();
    }

    private void setupWelcome(Employee employee) {
        welcomeLabel.setText("Welcome back, " + employee.getUserName());
        welcomeSub.setText(getRoleDescription(employee.getRole()));
        dateTimeLabel.setText(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy  \u2022  HH:mm")));
        roleDisplayLabel.setText(employee.getRole().toString() + " Access");
    }

    private String getRoleDescription(EmployeeRole role) {
        switch (role) {
            case MANAGER: return "You have full access to all banking operations.";
            case TELLER:  return "You can process deposits, withdrawals, and view transactions.";
            case CS:      return "You can manage customers and open new accounts.";
            default:      return "";
        }
    }

    private void loadStats() {
        List<Customer>    customers = bankService.getCustomers();
        List<Account>     accounts  = bankService.getAccounts();
        List<Transaction> txs       = bankService.getAllTransactions();

        BigDecimal total = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalCustomersLabel.setText(String.valueOf(customers.size()));
        totalAccountsLabel.setText(String.valueOf(accounts.size()));
        totalTransactionsLabel.setText(String.valueOf(txs.size()));
        totalBalanceLabel.setText("EGP " + String.format("%,.2f", total.doubleValue()));
    }

    private void setupQuickActions(Employee employee) {
        quickActionsBox.getChildren().clear();
        EmployeeRole role = employee.getRole();

        if (role == EmployeeRole.MANAGER || role == EmployeeRole.CS) {
            quickActionsBox.getChildren().add(
                    createActionButton("  Create Customer", "#1E4D8C",
                            () -> dashboardController.showCreateCustomer()));
            quickActionsBox.getChildren().add(
                    createActionButton("  Open Account", "#1E4D8C",
                            () -> dashboardController.showOpenAccount()));
        }
        if (role == EmployeeRole.MANAGER || role == EmployeeRole.TELLER) {
            quickActionsBox.getChildren().add(
                    createActionButton("  Deposit Funds", "#1A7A4A",
                            () -> dashboardController.showDeposit()));
            quickActionsBox.getChildren().add(
                    createActionButton("  Withdraw Funds", "#B7791F",
                            () -> dashboardController.showWithdraw()));
        }
        quickActionsBox.getChildren().add(
                createActionButton("  Transaction History", "#6B2C91",
                        () -> dashboardController.showTransactionHistory()));
    }

    private Button createActionButton(String text, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(40);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-font-size: 13px; -fx-cursor: hand; "
                + "-fx-alignment: CENTER_LEFT; -fx-padding: 0 16;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void setupPermissionBadges(Employee employee) {
        permissionsBox.getChildren().clear();
        EmployeeRole role = employee.getRole();

        if (role == EmployeeRole.MANAGER || role == EmployeeRole.CS) {
            permissionsBox.getChildren().add(createBadge("Create Customer", "#1E4D8C"));
            permissionsBox.getChildren().add(createBadge("Open Account",    "#1E4D8C"));
        }
        if (role == EmployeeRole.MANAGER || role == EmployeeRole.TELLER) {
            permissionsBox.getChildren().add(createBadge("Deposit",  "#1A7A4A"));
            permissionsBox.getChildren().add(createBadge("Withdraw", "#1A7A4A"));
        }
        permissionsBox.getChildren().add(createBadge("View Transactions", "#6B2C91"));
        if (role == EmployeeRole.MANAGER) {
            permissionsBox.getChildren().add(createBadge("Audit Log",  "#B7791F"));
            permissionsBox.getChildren().add(createBadge("Export CSV", "#B7791F"));
        }
    }

    private Label createBadge(String text, String color) {
        Label badge = new Label(text);
        badge.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; "
                + "-fx-background-radius: 10;");
        return badge;
    }

    private void setupRecentTransactionTable() {
        recColType.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getType().toString()));
        recColAmount.setCellValueFactory(data ->
                new SimpleStringProperty(
                        "EGP " + String.format("%,.2f", data.getValue().getAmount().doubleValue())));
        recColAccount.setCellValueFactory(data ->
                new SimpleStringProperty(
                        NumberFormatter.mask(data.getValue().getAccountNumber(), 4)));
        recColEmployee.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPerformedByEmployeeName()));
        recColTime.setCellValueFactory(data ->
                new SimpleStringProperty(
                        NumberFormatter.timeFormatter(data.getValue().getTimestamp())));

        // Color-code the type column
        recColType.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color = "Deposit".equals(item) ? "#1A7A4A" : "#C0392B";
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });
    }

    private void loadRecentTransactions() {
        List<Transaction> all = bankService.getAllTransactions();

        // Java-17-compatible reversal: copy + Collections.reverse
        List<Transaction> allCopy = new ArrayList<>(all);
        Collections.reverse(allCopy);

        // Take at most 10
        List<Transaction> recent = allCopy.size() > 10
                ? allCopy.subList(0, 10)
                : allCopy;

        recentTransactionTable.getItems().setAll(recent);
        recentTxCount.setText("Last " + recent.size() + " transactions");
    }
}
