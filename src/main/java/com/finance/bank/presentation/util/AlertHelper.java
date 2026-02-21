package com.finance.bank.presentation.util;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Utility for displaying inline alert banners within the UI.
 * Avoids modal dialogs for a more professional banking UX.
 */
public class AlertHelper {

    public enum AlertType { SUCCESS, ERROR, WARNING, INFO }

    /**
     * Creates a styled inline alert banner and appends it to the given container.
     * Auto-fades after 4 seconds for SUCCESS messages.
     */
    public static HBox createAlert(String message, AlertType type) {
        HBox alertBox = new HBox(10);
        alertBox.setAlignment(Pos.CENTER_LEFT);
        alertBox.setPadding(new Insets(12, 16, 12, 16));
        alertBox.setMaxWidth(Double.MAX_VALUE);

        String icon = switch (type) {
            case SUCCESS -> "✓";
            case ERROR   -> "✗";
            case WARNING -> "⚠";
            case INFO    -> "ℹ";
        };

        String styleClass = switch (type) {
            case SUCCESS -> "alert-success";
            case ERROR   -> "alert-error";
            case WARNING -> "alert-warning";
            case INFO    -> "alert-warning";
        };

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("alert-text");
        iconLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label msgLabel = new Label(message);
        msgLabel.getStyleClass().add("alert-text");
        msgLabel.setWrapText(true);
        HBox.setHgrow(msgLabel, Priority.ALWAYS);

        alertBox.getChildren().addAll(iconLabel, msgLabel);
        alertBox.getStyleClass().add(styleClass);

        // Auto-dismiss success alerts
        if (type == AlertType.SUCCESS) {
            FadeTransition fade = new FadeTransition(Duration.seconds(0.5), alertBox);
            fade.setDelay(Duration.seconds(3.5));
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                if (alertBox.getParent() instanceof VBox vbox) {
                    vbox.getChildren().remove(alertBox);
                }
            });
            fade.play();
        }

        return alertBox;
    }

    /**
     * Shows an alert in a VBox container, clearing previous alerts first.
     */
    public static void showAlert(VBox container, String message, AlertType type) {
        // Remove existing alerts
        container.getChildren().removeIf(node -> node instanceof HBox
                && (node.getStyleClass().contains("alert-success")
                    || node.getStyleClass().contains("alert-error")
                    || node.getStyleClass().contains("alert-warning")));

        HBox alert = createAlert(message, type);
        container.getChildren().add(0, alert);
    }
}
