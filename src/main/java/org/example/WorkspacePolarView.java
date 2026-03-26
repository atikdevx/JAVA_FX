package com.equationplotter.ui;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class WorkspacePolarView extends StackPane {

    private boolean isDark = false;

    public WorkspacePolarView(Runnable onBack) {
        GraphPolarCanvas graph = new GraphPolarCanvas();

        widthProperty().addListener((obs, ov, nv) -> { graph.setWidth(nv.doubleValue()); graph.draw(); });
        heightProperty().addListener((obs, ov, nv) -> { graph.setHeight(nv.doubleValue()); graph.draw(); });

        DraggablePolarEquationPanel overlay = new DraggablePolarEquationPanel(graph::setEquations);

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

        overlay.setManaged(false); backBtn.setManaged(false); homeBtn.setManaged(false); darkBtn.setManaged(false);

        getChildren().addAll(graph, overlay, backBtn, homeBtn, darkBtn);

        Platform.runLater(() -> {
            graph.setWidth(getWidth()); graph.setHeight(getHeight()); graph.draw();
            overlay.autosize(); overlay.relocate(20, 90);
            backBtn.autosize(); backBtn.relocate(20, 20);
            positionHomeButton(homeBtn); positionDarkButton(darkBtn);
        });

        widthProperty().addListener((o, oldW, newW) -> { positionHomeButton(homeBtn); positionDarkButton(darkBtn); });
        heightProperty().addListener((o, oldH, newH) -> positionDarkButton(darkBtn));
    }

    private void positionHomeButton(Button btn) { double w = getWidth(); if (w > 0) { btn.autosize(); btn.relocate(w - btn.getWidth() - 20, 20); } }
    private void positionDarkButton(Button btn) { double w = getWidth(), h = getHeight(); if (w > 0 && h > 0) { btn.autosize(); btn.relocate(w - btn.getWidth() - 20, h - btn.getHeight() - 20); } }

    private String getLightButtonStyle() { return "-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-text-fill: #444444; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"; }
    private String getDarkButtonStyle() { return "-fx-font-size: 14px; -fx-background-color: #2b2b2b; -fx-text-fill: #dddddd; -fx-border-color: #555555; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);"; }
}