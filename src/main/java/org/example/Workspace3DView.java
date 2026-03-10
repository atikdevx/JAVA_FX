package com.equationplotter.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class Workspace3DView extends StackPane {

    public Workspace3DView(Runnable onBack) {
        Graph3DPane graph3DPane = new Graph3DPane();
        Draggable3DEquationPanel panel = new Draggable3DEquationPanel(graph3DPane::setPlots);

        Button backBtn = new Button("← Back");
        backBtn.setStyle("""
            -fx-font-size: 14px;
            -fx-background-color: linear-gradient(to right, #9d00ff, #d329ff);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 10 18;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """);
        backBtn.setOnAction(e -> onBack.run());

        Button resetBtn = new Button("Reset Camera");
        resetBtn.setStyle("""
            -fx-font-size: 14px;
            -fx-background-color: rgba(255,255,255,0.96);
            -fx-text-fill: #333333;
            -fx-font-weight: bold;
            -fx-padding: 10 18;
            -fx-background-radius: 10;
            -fx-border-color: #dddddd;
            -fx-border-radius: 10;
            -fx-cursor: hand;
        """);
        resetBtn.setOnAction(e -> graph3DPane.resetCamera());

        panel.setManaged(false);
        backBtn.setManaged(false);
        resetBtn.setManaged(false);

        getChildren().addAll(graph3DPane, panel, backBtn, resetBtn);
        StackPane.setAlignment(graph3DPane, Pos.CENTER);

        Platform.runLater(() -> {
            panel.autosize();
            panel.relocate(20, 90);

            backBtn.autosize();
            backBtn.relocate(20, 20);

            resetBtn.autosize();
            resetBtn.relocate(getWidth() - resetBtn.getWidth() - 20, 20);
        });

        widthProperty().addListener((obs, oldVal, newVal) -> {
            resetBtn.autosize();
            resetBtn.relocate(getWidth() - resetBtn.getWidth() - 20, 20);
        });
    }
}