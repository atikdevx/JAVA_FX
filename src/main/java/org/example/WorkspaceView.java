package com.equationplotter.ui;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class WorkspaceView extends StackPane {

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
        // We pass the graph::setEquations method so the panel can update the graph
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
        homeBtn.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-background-color: #ffffff;" +
                        "-fx-text-fill: #444;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 16;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        );
        homeBtn.setOnAction(e -> graph.resetView());

        // 5. Absolute Positioning Setup
        // We disable 'managed' so we can place them exactly where we want (relocate)
        overlay.setManaged(false);
        backBtn.setManaged(false);
        homeBtn.setManaged(false);

        getChildren().addAll(graph, overlay, backBtn, homeBtn);

        // 6. Initial Layout & Positioning
        Platform.runLater(() -> {
            graph.setWidth(getWidth());
            graph.setHeight(getHeight());
            graph.draw();

            // [FIX] These calls ensure the panel calculates its size correctly before we move it
            overlay.applyCss();
            overlay.autosize();
            overlay.relocate(20, 90);

            backBtn.autosize();
            backBtn.relocate(20, 20);

            positionHomeButton(homeBtn);
        });

        // Keep Home button in top-right when resizing
        widthProperty().addListener((o, oldW, newW) -> positionHomeButton(homeBtn));
    }

    private void positionHomeButton(Button btn) {
        double w = getWidth();
        if (w > 0) {
            btn.autosize();
            // Position at Top-Right with 20px padding
            btn.relocate(w - btn.getWidth() - 20, 20);
        }
    }
}









//
//package com.equationplotter.ui;
//
//import javafx.application.Platform;
//import javafx.scene.control.Button; // 🔥 Button ইমপোর্ট করা হয়েছে
//import javafx.scene.layout.StackPane;
//
//public class WorkspaceView extends StackPane {
//
//    public WorkspaceView(Runnable onBack) {
//
//        GraphCanvas graph = new GraphCanvas();
//
//        // resize graph with window
//        widthProperty().addListener((obs, ov, nv) -> {
//            graph.setWidth(nv.doubleValue());
//            graph.draw();
//        });
//        heightProperty().addListener((obs, ov, nv) -> {
//            graph.setHeight(nv.doubleValue());
//            graph.draw();
//        });
//
//        // ✅ panel will call this when text/color changes
//        DraggableEquationPanel overlay = new DraggableEquationPanel(graph::setEquations);
//        Button backBtn = new Button("← Back");
//        backBtn.setStyle(
//                "-fx-font-size: 14px;" +
//                        "-fx-background-color: #9D00FF;" +
//                        "-fx-text-fill: white;" +
//                        "-fx-font-weight: bold;" +
//                        "-fx-padding: 8 16;" +
//                        "-fx-background-radius: 5;" +
//                        "-fx-cursor: hand;"
//        );
//        backBtn.setOnAction(e -> onBack.run());
//        overlay.setManaged(false);
//        backBtn.setManaged(false);
//        getChildren().addAll(graph, overlay, backBtn);
//
//        Platform.runLater(() -> {
//            graph.setWidth(getWidth());
//            graph.setHeight(getHeight());
//            graph.draw();
//
//            // ইকুয়েশন প্যানেলের পজিশন (যা আগে ছিল তাই আছে)
//            overlay.applyCss();
//            overlay.autosize();
//            overlay.relocate(20, 90);
//            backBtn.applyCss();
//            backBtn.autosize();
//            backBtn.relocate(20, 20);
//        });
//    }
//}
