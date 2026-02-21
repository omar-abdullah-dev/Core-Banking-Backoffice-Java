package com.finance.bank.app;

import com.finance.bank.presentation.util.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Finance Bank — JavaFX Desktop Application Entry Point
 *
 * Architecture:
 *   JavaFX UI  →  Service Layer  →  Repository Layer  →  Domain Models
 *
 * The presentation layer (JavaFX) ONLY calls the Service Layer.
 * Business logic is never duplicated in controllers.
 *
 * Run: mvn javafx:run
 * Or:  java --module-path <javafx-sdk-path>/lib --add-modules javafx.controls,javafx.fxml -jar banking-system.jar
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        NavigationManager nav = NavigationManager.getInstance();
        nav.initialize(primaryStage);
        nav.navigateTo("login.fxml");

        primaryStage.setTitle("Finance Bank — Back Office System v2.0");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
