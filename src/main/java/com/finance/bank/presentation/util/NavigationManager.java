package com.finance.bank.presentation.util;

import com.finance.bank.model.Employee;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Centralized navigation manager for the JavaFX application.
 * Handles all screen transitions and maintains application state.
 */
public class NavigationManager {

    private static NavigationManager instance;
    private Stage primaryStage;
    private Employee currentEmployee;

    private static final String CSS_PATH = "/com/finance/bank/presentation/css/bank-theme.css";
    private static final String VIEWS_BASE = "/com/finance/bank/presentation/views/";

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        if (instance == null) instance = new NavigationManager();
        return instance;
    }

    public void initialize(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Finance Bank — Back Office System");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(720);
    }

    public void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(VIEWS_BASE + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource(CSS_PATH).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlFile, e);
        }
    }

    public <T> T navigateToAndGetController(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(VIEWS_BASE + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource(CSS_PATH).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlFile, e);
        }
    }

    public Employee getCurrentEmployee() { return currentEmployee; }

    public void setCurrentEmployee(Employee employee) { this.currentEmployee = employee; }

    public Stage getPrimaryStage() { return primaryStage; }
}
