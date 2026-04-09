package com.equationplotter.ui;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class WorkspacePolarView extends StackPane {

    private boolean isDark = false;

    public WorkspacePolarView(Runnable onBack) {
        GraphPolarCanvas graph = new GraphPolarCanvas();

        // 1. Sync graph size with window
        widthProperty().addListener((obs, ov, nv) -> { graph.setWidth(nv.doubleValue()); graph.draw(); });
        heightProperty().addListener((obs, ov, nv) -> { graph.setHeight(nv.doubleValue()); graph.draw(); });

        DraggablePolarEquationPanel overlay = new DraggablePolarEquationPanel(graph::setEquations);

        // 2. Buttons Setup
        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #9D00FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5; -fx-cursor: hand;");
        backBtn.setOnAction(e -> onBack.run());

        Button homeBtn = new Button("⌂ Home");
        homeBtn.setStyle(getLightButtonStyle());
        homeBtn.setOnAction(e -> graph.resetView());

        Button darkBtn = new Button("🌙 Dark Mode");
        darkBtn.setStyle(getLightButtonStyle());
        darkBtn.setOnAction(e -> {
            isDark = !isDark;
            graph.setDarkMode(isDark);
            if (isDark) { darkBtn.setText("☀️ Light Mode"); darkBtn.setStyle(getDarkButtonStyle()); }
            else { darkBtn.setText("🌙 Dark Mode"); darkBtn.setStyle(getLightButtonStyle()); }
        });

        // ==========================================
        // 3. UI Layer Setup (Fixes the Disappearing Buttons Bug)
        // ==========================================
        AnchorPane uiLayer = new AnchorPane();
        uiLayer.setPickOnBounds(false); // Allows mouse clicks to pass through empty space to the graph

        // Pin the buttons to the corners (JavaFX will automatically handle the math)
        AnchorPane.setTopAnchor(backBtn, 20.0);
        AnchorPane.setLeftAnchor(backBtn, 20.0);

        AnchorPane.setTopAnchor(overlay, 80.0);
        AnchorPane.setLeftAnchor(overlay, 20.0);

        AnchorPane.setTopAnchor(homeBtn, 20.0);
        AnchorPane.setRightAnchor(homeBtn, 20.0);

        AnchorPane.setBottomAnchor(darkBtn, 20.0);
        AnchorPane.setRightAnchor(darkBtn, 20.0);

        uiLayer.getChildren().addAll(backBtn, overlay, homeBtn, darkBtn);

        // Add Graph (Bottom layer) and UI Layer (Top layer)
        getChildren().addAll(graph, uiLayer);

        // ==========================================
        // 4. KEYBOARD SHORTCUTS: Backspace/Delete/Esc to go back
        // ==========================================
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // Safety Check: If the user is typing in the equation box, DO NOT go back!
            if (event.getTarget() instanceof TextField) {
                return;
            }

            // If they press Backspace, Delete, or Esc, trigger the Back button
            if (event.getCode() == KeyCode.BACK_SPACE ||
                    event.getCode() == KeyCode.DELETE ||
                    event.getCode() == KeyCode.ESCAPE) {

                onBack.run();
                event.consume();
            }
        });

        // 5. Initial setup
        Platform.runLater(() -> {
            this.requestFocus(); // Ensures the window listens for keyboard inputs immediately
            graph.setWidth(getWidth());
            graph.setHeight(getHeight());
            graph.draw();
        });
    }

    private String getLightButtonStyle() { return "-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-text-fill: #444444; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"; }
    private String getDarkButtonStyle() { return "-fx-font-size: 14px; -fx-background-color: #2b2b2b; -fx-text-fill: #dddddd; -fx-border-color: #555555; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);"; }
}