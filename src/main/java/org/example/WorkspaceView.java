package com.equationplotter.ui;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class WorkspaceView extends StackPane {

    // Keep track of the current theme state
    private boolean isDark = false;

    public WorkspaceView(Runnable onBack) {

        GraphCanvas graph = new GraphCanvas();

        // 1. Sync graph size with window
        widthProperty().addListener((obs, ov, nv) -> {
            graph.setWidth(nv.doubleValue());
            graph.draw();
        });
        heightProperty().addListener((obs, ov, nv) -> {
            graph.setHeight(nv.doubleValue());
            graph.draw();
        });

        // 2. The equation editor panel (Left Bar)
        DraggableEquationPanel overlay = new DraggableEquationPanel(graph::setEquations);

        // 3. Back Button
        Button backBtn = new Button("← Back");
        backBtn.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-background-color: #9D00FF;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 16;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        backBtn.setOnAction(e -> onBack.run());

        // 4. Home (Reset) Button
        Button homeBtn = new Button("⌂ Home");
        homeBtn.setStyle(getLightButtonStyle());
        homeBtn.setOnAction(e -> graph.resetView());

        // 5. Dark Mode Toggle Button
        Button darkBtn = new Button("🌙 Dark Mode");
        darkBtn.setStyle(getLightButtonStyle());

        darkBtn.setOnAction(e -> {
            isDark = !isDark;
            graph.setDarkMode(isDark);

            if (isDark) {
                darkBtn.setText("☀️ Light Mode");
                darkBtn.setStyle(getDarkButtonStyle());
            } else {
                darkBtn.setText("🌙 Dark Mode");
                darkBtn.setStyle(getLightButtonStyle());
            }
        });

        // 6. Absolute Positioning Setup
        overlay.setManaged(false);
        backBtn.setManaged(false);
        homeBtn.setManaged(false);
        darkBtn.setManaged(false); // Disable management so we can pin it

        getChildren().addAll(graph, overlay, backBtn, homeBtn, darkBtn);

        // 7. Initial Layout & Positioning
        Platform.runLater(() -> {
            graph.setWidth(getWidth());
            graph.setHeight(getHeight());
            graph.draw();

            overlay.applyCss();
            overlay.autosize();
            overlay.relocate(20, 90);

            backBtn.autosize();
            backBtn.relocate(20, 20);

            positionHomeButton(homeBtn);
            positionDarkButton(darkBtn);
        });

        // Keep Home button in top-right and Dark button in bottom-right when resizing
        widthProperty().addListener((o, oldW, newW) -> {
            positionHomeButton(homeBtn);
            positionDarkButton(darkBtn);
        });

        heightProperty().addListener((o, oldH, newH) -> {
            positionDarkButton(darkBtn); // Bottom position depends on height!
        });
    }

    private void positionHomeButton(Button btn) {
        double w = getWidth();
        if (w > 0) {
            btn.autosize();
            // Position at Top-Right with 20px padding
            btn.relocate(w - btn.getWidth() - 20, 20);
        }
    }

    private void positionDarkButton(Button btn) {
        double w = getWidth();
        double h = getHeight();
        if (w > 0 && h > 0) {
            btn.autosize();
            // Position at Bottom-Right with 20px padding
            btn.relocate(w - btn.getWidth() - 20, h - btn.getHeight() - 20);
        }
    }

    // Helper methods for button styling to keep code clean
    private String getLightButtonStyle() {
        return "-fx-font-size: 14px;" +
                "-fx-background-color: #ffffff;" +
                "-fx-text-fill: #444444;" +
                "-fx-border-color: #cccccc;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;" +
                "-fx-padding: 8 16;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);";
    }

    private String getDarkButtonStyle() {
        return "-fx-font-size: 14px;" +
                "-fx-background-color: #2b2b2b;" +
                "-fx-text-fill: #dddddd;" +
                "-fx-border-color: #555555;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;" +
                "-fx-padding: 8 16;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);";
    }
}