package com.finance.bank.presentation.controllers;

import com.finance.bank.model.*;
import com.finance.bank.presentation.util.AlertHelper;
import com.finance.bank.service.BankService;
import com.finance.bank.util.NumberFormatter;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Account List screen.
 * Displays all accounts via BankService.getAccounts().
 * Summary stats show total, savings count, current count, and total balance.
 */
public class AccountListController implements Initializable, EmployeeAware {

    @FXML private TableView<Account>            accountTable;
    @FXML private TableColumn<Account, String>  colAccountNumber;
    @FXML private TableColumn<Account, String>  colType;
    @FXML private TableColumn<Account, String>  colBalance;
    @FXML private TableColumn<Account, String>  colOwner;
    @FXML private TableColumn<Account, String>  colNationalId;
    @FXML private TableColumn<Account, String>  colOverdraft;
    @FXML private TextField                     searchField;
    @FXML private ComboBox<String>              typeFilter;
    @FXML private Label                         accountCountLabel;
    @FXML private Label                         totalAccountsLabel;
    @FXML private Label                         savingsCountLabel;
    @FXML private Label                         currentCountLabel;
    @FXML private Label                         totalBalanceLabel;
    @FXML private VBox                          alertContainer;

    private final BankService bankService = BankService.getInstance();
    private ObservableList<Account> allAccounts;
    private FilteredList<Account>   filteredAccounts;

    @Override
    public void setEmployee(Employee employee) {
        loadAccounts();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupTypeFilter();
    }

    private void setupTableColumns() {
        colAccountNumber.setCellValueFactory(d ->
                new SimpleStringProperty(
                        NumberFormatter.mask(d.getValue().getAccountNumber(), 4)));

        colType.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getAccountType().label()));

        colBalance.setCellValueFactory(d ->
                new SimpleStringProperty(
                        "EGP " + String.format("%,.2f", d.getValue().getBalance().doubleValue())));

        colOwner.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOwner().getName()));

        colNationalId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOwner().getNationalId()));

        colOverdraft.setCellValueFactory(d -> {
            if (d.getValue() instanceof CurrentAccount) {
                CurrentAccount ca = (CurrentAccount) d.getValue();
                return new SimpleStringProperty(
                        "EGP " + String.format("%,.2f", ca.getOverdraftLimit().doubleValue()));
            }
            return new SimpleStringProperty("\u2014");
        });

        // Color-coded type badge
        colType.setCellFactory(col -> new TableCell<Account, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                boolean isSavings = "Savings".equals(item);
                badge.setStyle(
                        "-fx-background-color: " + (isSavings ? "#EBF0FA" : "#FEF3CD") + "; "
                        + "-fx-text-fill: " + (isSavings ? "#1E4D8C" : "#B7791F") + "; "
                        + "-fx-font-weight: bold; -fx-font-size: 11px; "
                        + "-fx-padding: 3 10; -fx-background-radius: 10;");
                setGraphic(badge);
                setText(null);
            }
        });

        colBalance.setStyle("-fx-alignment: CENTER_RIGHT;");
    }

    private void setupTypeFilter() {
        typeFilter.setItems(FXCollections.observableArrayList(
                "All Types", "Savings", "Current"));
        typeFilter.setValue("All Types");
    }

    private void loadAccounts() {
        List<Account> accounts = bankService.getAccounts();
        allAccounts      = FXCollections.observableArrayList(accounts);
        filteredAccounts = new FilteredList<>(allAccounts, p -> true);
        accountTable.setItems(filteredAccounts);
        updateStats(accounts);
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText().toLowerCase().trim();
        filteredAccounts.setPredicate(acc -> {
            if (q.isEmpty()) return true;
            return acc.getAccountNumber().contains(q)
                    || acc.getOwner().getName().toLowerCase().contains(q)
                    || acc.getOwner().getNationalId().contains(q);
        });
        accountCountLabel.setText(filteredAccounts.size() + " accounts");
    }

    @FXML
    private void handleTypeFilter() {
        String type = typeFilter.getValue();
        filteredAccounts.setPredicate(acc -> {
            if (type == null || "All Types".equals(type)) return true;
            return acc.getAccountType().label().equals(type);
        });
        accountCountLabel.setText(filteredAccounts.size() + " accounts");
    }

    @FXML
    private void handleRefresh() {
        loadAccounts();
        AlertHelper.showAlert(alertContainer, "Account list refreshed.", AlertHelper.AlertType.SUCCESS);
    }

    private void updateStats(List<Account> accounts) {
        long savings  = accounts.stream().filter(a -> a.getAccountType() == AccountType.SAVINGS).count();
        long current  = accounts.stream().filter(a -> a.getAccountType() == AccountType.CURRENT).count();
        BigDecimal total = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalAccountsLabel.setText(String.valueOf(accounts.size()));
        savingsCountLabel.setText(String.valueOf(savings));
        currentCountLabel.setText(String.valueOf(current));
        totalBalanceLabel.setText("EGP " + String.format("%,.2f", total.doubleValue()));
        accountCountLabel.setText(accounts.size() + " accounts");
    }
}
