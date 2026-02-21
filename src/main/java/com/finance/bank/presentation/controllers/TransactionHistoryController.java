package com.finance.bank.presentation.controllers;

import com.finance.bank.model.*;
import com.finance.bank.presentation.util.AlertHelper;
import com.finance.bank.service.BankService;
import com.finance.bank.util.NumberFormatter;
import com.finance.bank.util.TransactionPrinter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

public class TransactionHistoryController implements Initializable, EmployeeAware {

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colAmount;
    @FXML private TableColumn<Transaction, String> colBalance;
    @FXML private TableColumn<Transaction, String> colAccount;
    @FXML private TableColumn<Transaction, String> colEmployee;
    @FXML private TableColumn<Transaction, String> colRole;
    @FXML private TableColumn<Transaction, String> colTimestamp;
    @FXML private TableColumn<Transaction, String> colTxId;

    @FXML private TextField nationalIdFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Label txCountLabel;
    @FXML private Label totalDepositsLabel;
    @FXML private Label totalWithdrawalsLabel;
    @FXML private Label txCountStat;
    @FXML private VBox alertContainer;

    private final BankService bankService = BankService.getInstance();
    private ObservableList<Transaction> allTransactions;
    private FilteredList<Transaction> filteredTransactions;
    private Employee currentEmployee;
    private String currentNationalIdFilter = null;

    @Override
    public void setEmployee(Employee employee) {
        this.currentEmployee = employee;
        loadAllTransactions();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupTypeFilter();
    }

    private void setupTableColumns() {
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType().toString()));
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(
                "EGP " + String.format("%,.2f", d.getValue().getAmount().doubleValue())));
        colBalance.setCellValueFactory(d -> new SimpleStringProperty(
                "EGP " + String.format("%,.2f", d.getValue().getBalanceAfter().doubleValue())));
        colAccount.setCellValueFactory(d -> new SimpleStringProperty(
                NumberFormatter.mask(d.getValue().getAccountNumber(), 4)));
        colEmployee.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPerformedByEmployeeName()));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPerformedByRole().toString()));
        colTimestamp.setCellValueFactory(d -> new SimpleStringProperty(
                NumberFormatter.timeFormatter(d.getValue().getTimestamp())));
        colTxId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransactionId()));

        // Color type column
        colType.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                Label badge = new Label(item);
                String bc = item.equals("Deposit") ? "#E8F5EE" : "#FDECEA";
                String tc = item.equals("Deposit") ? "#1A7A4A" : "#C0392B";
                badge.setStyle("-fx-background-color: " + bc + "; -fx-text-fill: " + tc + "; " +
                               "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 10; " +
                               "-fx-background-radius: 10;");
                setGraphic(badge);
                setText(null);
            }
        });

        // Right-align amounts
        colAmount.setStyle("-fx-alignment: CENTER_RIGHT;");
        colBalance.setStyle("-fx-alignment: CENTER_RIGHT;");
    }

    private void setupTypeFilter() {
        typeFilter.setItems(FXCollections.observableArrayList("All Types", "Deposit", "Withdrawal"));
        typeFilter.setValue("All Types");
    }

    private void loadAllTransactions() {
        List<Transaction> txs = bankService.getAllTransactions();
        // Reverse to show newest first
        List<Transaction> reversed = new ArrayList<>(txs);
        Collections.reverse(reversed);
        allTransactions = FXCollections.observableArrayList(reversed);
        filteredTransactions = new FilteredList<>(allTransactions, p -> true);
        transactionTable.setItems(filteredTransactions);
        updateStats(reversed);
    }

    @FXML
    private void handleFilterByCustomer() {
        String nationalId = nationalIdFilter.getText().trim();
        if (nationalId.isEmpty()) {
            showError("Please enter a National ID.");
            return;
        }
        Customer customer = bankService.findCustomerByNationalId(nationalId);
        if (customer == null) {
            showError("No customer found with this National ID.");
            return;
        }

        currentNationalIdFilter = nationalId;
        List<Transaction> customerTxs = new ArrayList<>();
        customer.getAccounts().forEach(acc ->
                customerTxs.addAll(bankService.getTransactionsByAccount(acc.getAccountNumber())));
        Collections.reverse(customerTxs);

        allTransactions.setAll(customerTxs);
        applyTypeFilter();
        updateStats(customerTxs);
        AlertHelper.showAlert(alertContainer,
                "Showing " + customerTxs.size() + " transactions for " + customer.getName(),
                AlertHelper.AlertType.SUCCESS);
    }

    @FXML
    private void handleTypeFilter() { applyTypeFilter(); }

    @FXML
    private void handleShowAll() {
        nationalIdFilter.clear();
        typeFilter.setValue("All Types");
        currentNationalIdFilter = null;
        loadAllTransactions();
        AlertHelper.showAlert(alertContainer, "Showing all transactions.", AlertHelper.AlertType.SUCCESS);
    }

    @FXML
    private void handleRefresh() {
        if (currentNationalIdFilter != null) handleFilterByCustomer();
        else loadAllTransactions();
    }

    @FXML
    private void handleExport() {
        if (filteredTransactions.isEmpty()) {
            showError("No transactions to export.");
            return;
        }
        try {
            String accountNumber = filteredTransactions.get(0).getAccountNumber();
            TransactionPrinter.exportNewTransactions(accountNumber, new ArrayList<>(filteredTransactions));
            AlertHelper.showAlert(alertContainer, "Transactions exported to CSV successfully.",
                    AlertHelper.AlertType.SUCCESS);
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    private void applyTypeFilter() {
        String type = typeFilter.getValue();
        filteredTransactions.setPredicate(tx -> {
            if (type == null || type.equals("All Types")) return true;
            return tx.getType().toString().equals(type);
        });
        updateStats(new ArrayList<>(filteredTransactions));
    }

    private void updateStats(List<Transaction> txs) {
        BigDecimal deposits = txs.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal withdrawals = txs.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAWAL)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalDepositsLabel.setText("EGP " + String.format("%,.2f", deposits.doubleValue()));
        totalWithdrawalsLabel.setText("EGP " + String.format("%,.2f", withdrawals.doubleValue()));
        txCountLabel.setText(txs.size() + " transactions");
        txCountStat.setText(String.valueOf(txs.size()));
    }

    private void showError(String msg) { AlertHelper.showAlert(alertContainer, msg, AlertHelper.AlertType.ERROR); }
}
