package com.finance.bank.presentation.controllers;

import com.finance.bank.exception.AuthenticationException;
import com.finance.bank.model.Employee;
import com.finance.bank.presentation.util.AlertHelper;
import com.finance.bank.presentation.util.NavigationManager;
import com.finance.bank.presentation.util.SessionManager;
import com.finance.bank.service.AuthenticationService;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Login screen controller.
 * Authenticates via AuthenticationService.
 * On success, stores Employee in SessionManager and navigates to dashboard.
 *
 * Production credentials (from AuthenticationService):
 *   ahmed    / ahmedPass!   (Manager)
 *   mohamed  / mohamedPass! (Teller)
 *   omar     / omarPass!    (Customer Service)
 *
 * Test credentials:
 *   manager  / manager123
 *   teller   / teller123
 *   cs       / cs123456
 */
public class LoginController implements Initializable {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private VBox          loginPanel;
    @FXML private VBox          alertContainer;
    @FXML private Region        alertSpacer;

    private final AuthenticationService authService = new AuthenticationService();
    private final NavigationManager     nav         = NavigationManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Entrance animation: fade in + slide up
        loginPanel.setOpacity(0);
        loginPanel.setTranslateY(24);

        FadeTransition fade = new FadeTransition(Duration.millis(450), loginPanel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        TranslateTransition slide = new TranslateTransition(Duration.millis(450), loginPanel);
        slide.setFromY(24);
        slide.setToY(0);
        slide.play();
    }

    @FXML
    private void handleLogin() {
        clearAlerts();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showError("Please enter your username.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Signing in...");

        try {
            Employee employee = authService.login(username, password);

            // Store authenticated employee in SessionManager
            SessionManager.setEmployee(employee);

            // Navigate to dashboard — DashboardController.initialize() reads SessionManager
            nav.navigateTo("dashboard.fxml");

        } catch (AuthenticationException e) {
            showError("Invalid username or password. Please try again.");
            passwordField.clear();
            passwordField.requestFocus();
            shakePanel();
        } finally {
            loginButton.setDisable(false);
            loginButton.setText("Sign In");
        }
    }

    private void showError(String message) {
        alertContainer.getChildren().clear();
        if (alertSpacer != null) alertSpacer.setPrefHeight(8);
        alertContainer.getChildren().add(
                AlertHelper.createAlert(message, AlertHelper.AlertType.ERROR));
    }

    private void clearAlerts() {
        alertContainer.getChildren().clear();
        if (alertSpacer != null) alertSpacer.setPrefHeight(0);
    }

    private void shakePanel() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(55), loginPanel);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setFromX(0);
        shake.setToX(8);
        shake.play();
    }
}
