package com.finance.bank.presentation.controllers;

import com.finance.bank.model.Employee;
import com.finance.bank.model.EmployeeRole;
import com.finance.bank.presentation.util.NavigationManager;
import com.finance.bank.presentation.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Main shell controller.
 * Manages the sidebar navigation and swaps content panels dynamically.
 * Employee is read from SessionManager — never passed via constructor.
 */
public class DashboardController {

    // Sidebar nav buttons
    @FXML private Button navDashboard;
    @FXML private Button navCreateCustomer;
    @FXML private Button navCustomerList;
    @FXML private Button navOpenAccount;
    @FXML private Button navAccountList;
    @FXML private Button navDeposit;
    @FXML private Button navWithdraw;
    @FXML private Button navTransactionHistory;
    @FXML private Button navAuditLog;

    // Sidebar section labels (shown/hidden per role)
    @FXML private Label navSectionCustomers;
    @FXML private Label navSectionAccounts;
    @FXML private Label navSectionTransactions;
    @FXML private Label navSectionAudit;

    // Top bar
    @FXML private Label pageTitle;
    @FXML private Label pageBreadcrumb;
    @FXML private Label topBarEmployeeName;
    @FXML private Label topBarRoleBadge;

    // Sidebar employee info
    @FXML private Label sidebarEmployeeName;
    @FXML private Label sidebarEmployeeRole;

    // Content container
    @FXML private StackPane contentContainer;

    private Button activeNavButton;
    private final NavigationManager nav = NavigationManager.getInstance();

    private static final String VIEWS_BASE = "/com/finance/bank/presentation/views/";

    /**
     * Called by JavaFX after FXML loading.
     * Employee is retrieved from SessionManager.
     */
    @FXML
    public void initialize() {
        Employee employee = SessionManager.getEmployee();
        if (employee == null) {
            nav.navigateTo("login.fxml");
            return;
        }
        setupEmployeeDisplay(employee);
        setupRoleBasedNavigation(employee);
        showDashboard();
    }

    private void setupEmployeeDisplay(Employee employee) {
        topBarEmployeeName.setText(employee.getUserName());
        sidebarEmployeeName.setText(employee.getUserName());
        sidebarEmployeeRole.setText(employee.getRole().toString());

        String roleClass;
        switch (employee.getRole()) {
            case MANAGER: roleClass = "employee-role-badge role-manager"; break;
            case TELLER:  roleClass = "employee-role-badge role-teller";  break;
            default:      roleClass = "employee-role-badge role-cs";       break;
        }
        topBarRoleBadge.getStyleClass().setAll(roleClass.split(" "));
        topBarRoleBadge.setText(employee.getRole().toString().toUpperCase());
    }

    private void setupRoleBasedNavigation(Employee employee) {
        EmployeeRole role = employee.getRole();
        boolean isManagerOrCS     = (role == EmployeeRole.MANAGER || role == EmployeeRole.CS);
        boolean isManagerOrTeller = (role == EmployeeRole.MANAGER || role == EmployeeRole.TELLER);
        boolean isManager         = (role == EmployeeRole.MANAGER);

        // Customer section — CS + Manager only
        setNavVisible(navSectionCustomers, isManagerOrCS);
        setNavVisible(navCreateCustomer,   isManagerOrCS);
        setNavVisible(navCustomerList,     isManagerOrCS);

        // Account section — Open account: CS + Manager; List: all
        setNavVisible(navSectionAccounts, true);
        setNavVisible(navOpenAccount,     isManagerOrCS);
        setNavVisible(navAccountList,     true);

        // Transaction section
        setNavVisible(navSectionTransactions,  true);
        setNavVisible(navDeposit,             isManagerOrTeller);
        setNavVisible(navWithdraw,            isManagerOrTeller);
        setNavVisible(navTransactionHistory,  true);

        // Audit — Manager only
        setNavVisible(navSectionAudit, isManager);
        setNavVisible(navAuditLog,     isManager);
    }

    private void setNavVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    // ── Navigation Handlers ──────────────────────────────────────

    @FXML
    public void showDashboard() {
        loadContent("dashboard_home.fxml", "Dashboard", "Overview", navDashboard);
    }

    @FXML
    public void showCreateCustomer() {
        if (!canAccess(EmployeeRole.MANAGER, EmployeeRole.CS)) return;
        loadContent("customer_form.fxml", "Create Customer", "Customers / Create", navCreateCustomer);
    }

    @FXML
    public void showCustomerList() {
        if (!canAccess(EmployeeRole.MANAGER, EmployeeRole.CS)) return;
        loadContent("customer_list.fxml", "Customer List", "Customers / All", navCustomerList);
    }

    @FXML
    public void showOpenAccount() {
        if (!canAccess(EmployeeRole.MANAGER, EmployeeRole.CS)) return;
        loadContent("account_form.fxml", "Open Account", "Accounts / Open", navOpenAccount);
    }

    @FXML
    public void showAccountList() {
        loadContent("account_list.fxml", "Account List", "Accounts / All", navAccountList);
    }

    @FXML
    public void showDeposit() {
        if (!canAccess(EmployeeRole.MANAGER, EmployeeRole.TELLER)) return;
        loadTransactionForm("DEPOSIT", "Deposit", "Transactions / Deposit", navDeposit);
    }

    @FXML
    public void showWithdraw() {
        if (!canAccess(EmployeeRole.MANAGER, EmployeeRole.TELLER)) return;
        loadTransactionForm("WITHDRAW", "Withdraw", "Transactions / Withdraw", navWithdraw);
    }

    @FXML
    public void showTransactionHistory() {
        loadContent("transaction_history.fxml", "Transaction History", "Transactions / History", navTransactionHistory);
    }

    @FXML
    public void showAuditLog() {
        if (!canAccess(EmployeeRole.MANAGER)) return;
        loadContent("transaction_history.fxml", "Audit Log", "Audit / All", navAuditLog);
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        nav.navigateTo("login.fxml");
    }

    // ── Private Helpers ──────────────────────────────────────────

    /**
     * Loads a generic content panel.
     * Controllers that implement EmployeeAware receive setEmployee() automatically.
     */
    private void loadContent(String fxmlFile, String title, String breadcrumb, Button navBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(VIEWS_BASE + fxmlFile));
            Node content = loader.load();

            Object controller = loader.getController();

            // Inject employee via EmployeeAware interface
            if (controller instanceof EmployeeAware) {
                ((EmployeeAware) controller).setEmployee(SessionManager.getEmployee());
            }

            // DashboardHome needs a reference back to this controller for quick-action buttons
            if (controller instanceof DashboardHomeController) {
                ((DashboardHomeController) controller).initializeDashboard(this);
            }

            contentContainer.getChildren().setAll(content);
            setPageHeader(title, "Finance Bank / " + breadcrumb);
            setActiveNav(navBtn);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load: " + fxmlFile, e);
        }
    }

    /**
     * Loads the transaction form with a mode parameter (DEPOSIT or WITHDRAW).
     * Uses TransactionFormController.initializeForm() to set the mode.
     */
    private void loadTransactionForm(String mode, String title, String breadcrumb, Button navBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(VIEWS_BASE + "transaction_form.fxml"));
            Node content = loader.load();
            TransactionFormController controller = loader.getController();
            controller.initializeForm(SessionManager.getEmployee(), mode);

            contentContainer.getChildren().setAll(content);
            setPageHeader(title, "Finance Bank / " + breadcrumb);
            setActiveNav(navBtn);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load transaction form", e);
        }
    }

    private void setPageHeader(String title, String breadcrumb) {
        pageTitle.setText(title);
        pageBreadcrumb.setText(breadcrumb);
    }

    private void setActiveNav(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("active");
        }
        if (button != null) {
            button.getStyleClass().add("active");
            activeNavButton = button;
        }
    }

    private boolean canAccess(EmployeeRole... allowedRoles) {
        Employee emp = SessionManager.getEmployee();
        if (emp == null) return false;
        for (EmployeeRole role : allowedRoles) {
            if (emp.getRole() == role) return true;
        }
        return false;
    }
}
