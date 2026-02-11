package com.equationplotter.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class DraggableEquationPanel extends VBox {

    private double dragOffsetX;
    private double dragOffsetY;

    private final VBox listBox = new VBox(10);
    private final Button addBtn = new Button("+ Add equation");

    private boolean minimized = false;

    private final HBox header = new HBox(10);
    private final Button minBtn = new Button("▾");

    private static final double EXPANDED_W = 360;
    private static final double MIN_W = 180;

    public DraggableEquationPanel() {

        // ✅ VBox children যেন full width নেয়
        setFillWidth(true);

        // expanded size
        setPrefWidth(EXPANDED_W);
        setMinWidth(EXPANDED_W);
        setMaxWidth(EXPANDED_W);

        setSpacing(10);
        setPadding(new Insets(12));
        setStyleExpanded();

        // ----- Header -----
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(9, 12, 9, 12));
        header.setCursor(Cursor.MOVE);
        header.setMaxWidth(Double.MAX_VALUE); // ✅ header full width

        header.setBackground(new Background(new BackgroundFill(
                Color.rgb(245, 245, 245, 0.90), new CornerRadii(12), Insets.EMPTY
        )));

        Label title = new Label("Equations");
        title.setStyle("-fx-font-weight: 700; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        minBtn.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-padding: 4 10;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: rgba(0,0,0,0.10);" +
                        "-fx-border-radius: 10;"
        );

        header.getChildren().addAll(title, spacer, minBtn);

        // ✅ Drag (works even minimized)
        header.setOnMousePressed(e -> {
            dragOffsetX = e.getX();
            dragOffsetY = e.getY();
        });

        header.setOnMouseDragged(e -> {
            double newX = getLayoutX() + (e.getX() - dragOffsetX);
            double newY = getLayoutY() + (e.getY() - dragOffsetY);

            if (getParent() != null) {
                double pw = getParent().getLayoutBounds().getWidth();
                double ph = getParent().getLayoutBounds().getHeight();

                double myW = getBoundsInParent().getWidth();
                double myH = getBoundsInParent().getHeight();

                newX = Math.max(0, Math.min(newX, pw - myW));
                newY = Math.max(0, Math.min(newY, ph - myH));
            }

            relocate(newX, newY);
        });

        // ----- Content -----
        addBtn.setMaxWidth(Double.MAX_VALUE); // ✅ full width button
        addBtn.setStyle("-fx-padding: 10 12; -fx-background-radius: 10;");
        addBtn.setOnAction(e -> addEquationField());

        listBox.setFillWidth(true);
        listBox.setMaxWidth(Double.MAX_VALUE); // ✅ list full width
        listBox.setPadding(new Insets(4, 2, 2, 2));

        // first field
        addEquationField();

        // minimize toggle
        minBtn.setOnAction(e -> toggleMinimize());

        getChildren().setAll(header, addBtn, listBox);
    }

    private void toggleMinimize() {
        minimized = !minimized;

        if (minimized) {
            minBtn.setText("▸");

            // minimized = only header pill, no big white overlay
            setStyleMinimized();
            setPadding(Insets.EMPTY);
            setSpacing(0);

            setPrefWidth(MIN_W);
            setMinWidth(MIN_W);
            setMaxWidth(MIN_W);

            getChildren().setAll(header);
            setPickOnBounds(false);

        } else {
            minBtn.setText("▾");

            setPickOnBounds(true);

            setPrefWidth(EXPANDED_W);
            setMinWidth(EXPANDED_W);
            setMaxWidth(EXPANDED_W);

            setPadding(new Insets(12));
            setSpacing(10);

            setStyleExpanded();
            getChildren().setAll(header, addBtn, listBox);
        }

        // ✅ important: CSS/layout refresh
        applyCss();
        layout();
    }

    private void setStyleExpanded() {
        setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: rgba(0,0,0,0.10);" +
                        "-fx-border-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0.2, 0, 6);"
        );
    }

    private void setStyleMinimized() {
        setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }

    private void addEquationField() {
        TextField tf = new TextField();
        tf.setPromptText("y = ...  (e.g. x^2, sin(x))");

        // ✅ make it BIG (width + height)
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setPrefHeight(38);
        tf.setStyle("-fx-background-radius: 10; -fx-padding: 9 10;");

        listBox.getChildren().add(tf);

        // ✅ refresh layout so it expands instantly
        applyCss();
        layout();
    }
}
