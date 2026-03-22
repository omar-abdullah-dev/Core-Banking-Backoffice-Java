package com.finance.bank.presentation.controllers;

import com.finance.bank.model.Account;
import com.finance.bank.model.Customer;
import com.finance.bank.model.Employee;
import com.finance.bank.presentation.util.AlertHelper;
import com.finance.bank.service.BankService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Customer List screen.
 * Displays all customers from BankService.getCustomers().
 * Supports live search (name / National ID) and sorting.
 */
public class CustomerListController implements Initializable, EmployeeAware {

    @FXML private TableView<Customer>          customerTable;
    @FXML private TableColumn<Customer, Number> colIndex;
    @FXML private TableColumn<Customer, String> colSystemId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colNationalId;
    @FXML private TableColumn<Customer, Number> colAccounts;
    @FXML private TableColumn<Customer, String> colActions;
    @FXML private TextField                     searchField;
    @FXML private ComboBox<String>              sortCombo;
    @FXML private Label                         customerCountLabel;
    @FXML private VBox                          alertContainer;

    private final BankService bankService = BankService.getInstance();
    private ObservableList<Customer> allCustomers;
    private FilteredList<Customer>   filteredCustomers;

    @Override
    public void setEmployee(Employee employee) {
        // Employee available from SessionManager if needed.
        loadCustomers();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupSortCombo();
    }

    private void setupTableColumns() {

        // Row index (1-based)
        colIndex.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        customerTable.getItems().indexOf(data.getValue()) + 1));

        // Shorten system ID for display
        colSystemId.setCellValueFactory(data ->
                new SimpleStringProperty(shortenId(data.getValue().getSystemId())));

        colName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName()));

        colNationalId.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNationalId()));

        colAccounts.setCellValueFactory(data ->{
            long count = bankService.getAccounts().stream()
                .filter(a -> a.getOwner().getSystemId()
                        .equals(data.getValue().getSystemId()))
                .count();
            return new SimpleIntegerProperty((int) count);});

        // Account count badge cell
        colAccounts.setCellFactory(col -> new TableCell<Customer, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(String.valueOf(item.intValue()));
                String styleClass = item.intValue() == 0 ? "badge badge-gray" : "badge badge-blue";
                badge.getStyleClass().setAll(styleClass.split(" "));
                setGraphic(badge);
                setText(null);
            }
        });

        // Actions column — View button
        colActions.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Customer customer = getTableView().getItems().get(getIndex());
                Button viewBtn = new Button("View");
                viewBtn.setStyle(
                    "-fx-background-color: #EBF0FA; -fx-text-fill: #1E4D8C; "
                    + "-fx-background-radius: 6; -fx-cursor: hand; "
                    + "-fx-font-size: 11px; -fx-padding: 4 10;");
                viewBtn.setOnAction(e -> showCustomerDetail(customer));
                setGraphic(viewBtn);
                setText(null);
            }
        });
    }

    private void setupSortCombo() {
        sortCombo.setItems(FXCollections.observableArrayList(
                "Name (A-Z)", "Name (Z-A)", "Most Accounts"));
        sortCombo.setValue("Name (A-Z)");
    }

    private void loadCustomers() {
        List<Customer> customers = bankService.getCustomers();
        allCustomers      = FXCollections.observableArrayList(customers);
        filteredCustomers = new FilteredList<>(allCustomers, p -> true);
        customerTable.setItems(filteredCustomers);
        customerCountLabel.setText(customers.size() + " customers");
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        filteredCustomers.setPredicate(customer -> {
            if (query.isEmpty()) return true;
            return customer.getName().toLowerCase().contains(query)
                    || customer.getNationalId().contains(query);
        });
        customerCountLabel.setText(filteredCustomers.size() + " customers");
    }

    @FXML
    private void handleSort() {
        String sort = sortCombo.getValue();
        if (sort == null || allCustomers == null) return;

        List<Customer> sorted;
        switch (sort) {
            case "Name (Z-A)":
                sorted = new ArrayList<>(allCustomers);
                sorted.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                break;
            case "Most Accounts":
                sorted = new ArrayList<>(allCustomers);
                List<Account> allAcc = bankService.getAccounts();
                sorted.sort((a, b) -> {
                    long countA = allAcc.stream().filter(ac -> ac.getOwner().getSystemId().equals(a.getSystemId())).count();
                    long countB = allAcc.stream().filter(ac -> ac.getOwner().getSystemId().equals(b.getSystemId())).count();
                    return Long.compare(countB, countA);
                });
                break;
            default: // "Name (A-Z)"
                sorted = new ArrayList<>(allCustomers);
                sorted.sort(Comparator.comparing(c -> c.getName().toLowerCase()));
                break;
        }
        allCustomers.setAll(sorted);
    }

    @FXML
    private void handleRefresh() {
        loadCustomers();
        AlertHelper.showAlert(alertContainer, "Customer list refreshed.", AlertHelper.AlertType.SUCCESS);
    }

    private void showCustomerDetail(Customer customer) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Customer Details");
        dialog.setHeaderText("Customer: " + customer.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("System ID   : ").append(customer.getSystemId()).append("\n");
        sb.append("National ID : ").append(customer.getNationalId()).append("\n");
        List<Account> customerAccounts = bankService.getAccounts().stream()
                .filter(a -> a.getOwner().getSystemId().equals(customer.getSystemId()))
                .toList();

        sb.append("Accounts    : ").append(customerAccounts.size()).append("\n\n");

        if (!customerAccounts.isEmpty()) {
            sb.append("Account details:\n");
            for (Account acc : customerAccounts) {
                sb.append("  \u2022 ").append(acc.getAccountType().label())
                  .append("  |  ").append(acc.getAccountNumber())
                  .append("  |  EGP ")
                  .append(String.format("%,.2f", acc.getBalance().doubleValue()))
                  .append("\n");
            }
        }

        dialog.setContentText(sb.toString());
        dialog.showAndWait();
    }

    private String shortenId(String id) {
        if (id == null || id.length() <= 20) return id;
        return id.substring(0, 20) + "...";
    }
}
