package com.equationplotter.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class WorkspaceView extends StackPane {

    private Runnable onBack;

    public WorkspaceView(Runnable onBack) {
        this.onBack = onBack;
        buildUI();
    }

    private void buildUI() {
        // ১. গ্রাফ তৈরি
        GraphCanvas graph = new GraphCanvas();
        // গ্রাফ যেন পুরো উইন্ডো জুড়ে থাকে
        graph.widthProperty().bind(widthProperty());
        graph.heightProperty().bind(heightProperty());

        // ২. বাটন তৈরি
        Button backBtn = new Button("← Back");

        // ৩. বাটনের স্টাইল (লাল রঙের বাটন যাতে চোখে পড়ে)
        backBtn.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-background-color: #ff4444;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 16;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );

        backBtn.setOnAction(e -> onBack.run());

        // ৪. লেআউটে যোগ করা (সবচেয়ে গুরুত্বপূর্ণ লাইন)
        // graph আগে দিতে হবে, backBtn পরে দিতে হবে
        getChildren().addAll(graph, backBtn);

        // ৫. বাটনটিকে বাম কোণায় সেট করা
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);

        // ৬. একদম কোণা থেকে একটু ফাঁকা রাখা (Margin)
        StackPane.setMargin(backBtn, new Insets(15, 0, 0, 15));
    }
}