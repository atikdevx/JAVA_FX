package com.equationplotter.ui;

import javafx.application.Platform;
import javafx.scene.control.Button; // 🔥 Button ইমপোর্ট করা হয়েছে
import javafx.scene.layout.StackPane;

public class WorkspaceView extends StackPane {

    public WorkspaceView(Runnable onBack) {

        GraphCanvas graph = new GraphCanvas();

        // resize graph with window
        widthProperty().addListener((obs, ov, nv) -> {
            graph.setWidth(nv.doubleValue());
            graph.draw();
        });
        heightProperty().addListener((obs, ov, nv) -> {
            graph.setHeight(nv.doubleValue());
            graph.draw();
        });

        // ✅ panel will call this when text/color changes
        DraggableEquationPanel overlay = new DraggableEquationPanel(graph::setEquations);
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

        // 🔥 ২. বাটন এবং ওভারলে দুটোরই managed প্রপার্টি false করে দেওয়া হলো
        // (যাতে এরা গ্রাফ বা একে অন্যের লেআউট নষ্ট না করে)
        overlay.setManaged(false);
        backBtn.setManaged(false);

        // 🔥 ৩. লেআউটে যোগ করা হলো (graph সবার আগে, যাতে এটি নিচে থাকে)
        getChildren().addAll(graph, overlay, backBtn);

        Platform.runLater(() -> {
            graph.setWidth(getWidth());
            graph.setHeight(getHeight());
            graph.draw();

            // ইকুয়েশন প্যানেলের পজিশন (যা আগে ছিল তাই আছে)
            overlay.applyCss();
            overlay.autosize();
            overlay.relocate(20, 90);

            // 🔥 ৪. ব্যাক বাটনের পজিশন সেট করা হলো
            // (X=20, Y=20 তে রাখা হয়েছে, যাতে ইকুয়েশন প্যানেলের ঠিক উপরে থাকে)
            backBtn.applyCss();
            backBtn.autosize();
            backBtn.relocate(20, 20);
        });
    }
}
//package com.equationplotter.ui;
//
//import javafx.application.Platform;
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
//
//        overlay.setManaged(false);
//        getChildren().addAll(graph, overlay);
//
//        Platform.runLater(() -> {
//            graph.setWidth(getWidth());
//            graph.setHeight(getHeight());
//            graph.draw();
//
//            overlay.applyCss();
//            overlay.autosize();
//            overlay.relocate(20, 90);
//        });
//    }
//}
