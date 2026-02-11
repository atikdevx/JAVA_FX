package com.equationplotter.ui;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DraggableEquationPanel extends VBox {

    private double dragOffsetX, dragOffsetY;

    private final VBox rowsBox = new VBox(10);
    private final Button addBtn = new Button("+ Add equation");

    private final HBox header = new HBox(10);
    private final Button minBtn = new Button("▾");
    private boolean minimized = false;

    private static final double EXPANDED_W = 380;
    private static final double MIN_W = 180;

    private final Consumer<List<PlotEquation>> onChange;
    private final PauseTransition debounce = new PauseTransition(Duration.millis(180));

    public DraggableEquationPanel(Consumer<List<PlotEquation>> onChange) {
        this.onChange = onChange;

        setFillWidth(true);
        setPrefWidth(EXPANDED_W);
        setMinWidth(EXPANDED_W);
        setMaxWidth(EXPANDED_W);

        setSpacing(10);
        setPadding(new Insets(12));
        setStyleExpanded();

        // ----- header -----
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(9, 12, 9, 12));
        header.setCursor(Cursor.MOVE);
        header.setMaxWidth(Double.MAX_VALUE);
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

        // drag
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

        // content
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle("-fx-padding: 10 12; -fx-background-radius: 10;");
        addBtn.setOnAction(e -> addRow(null, null));

        rowsBox.setFillWidth(true);
        rowsBox.setMaxWidth(Double.MAX_VALUE);
        rowsBox.setPadding(new Insets(2));

        // debounce update
        debounce.setOnFinished(e -> pushUpdate());

        // minimize
        minBtn.setOnAction(e -> toggleMinimize());

        getChildren().setAll(header, addBtn, rowsBox);

        // first row
        addRow("y=4x", Color.web("#2563eb"));
        pushUpdate();
    }

    private void toggleMinimize() {
        minimized = !minimized;

        if (minimized) {
            minBtn.setText("▸");
            setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
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

            getChildren().setAll(header, addBtn, rowsBox);
        }

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

    private void addRow(String initialText, Color initialColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        ColorPicker picker = new ColorPicker(initialColor != null ? initialColor : randomColor());
        picker.setPrefWidth(55);

        TextField tf = new TextField(initialText != null ? initialText : "");
        tf.setPromptText("y = ... (x^2, sin(x), 5x+1)");
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setPrefHeight(36);
        tf.setStyle("-fx-background-radius: 10; -fx-padding: 9 10;");
        HBox.setHgrow(tf, Priority.ALWAYS);

        Button del = new Button("✕");
        del.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-padding: 6 10;" +
                        "-fx-background-color: rgba(239,68,68,0.12);" +
                        "-fx-text-fill: #b91c1c;"
        );

        // events -> update graph
        tf.textProperty().addListener((o,a,b) -> debounce.playFromStart());
        picker.valueProperty().addListener((o,a,b) -> debounce.playFromStart());
        del.setOnAction(e -> {
            rowsBox.getChildren().remove(row);
            pushUpdate();
        });

        row.getChildren().addAll(picker, tf, del);
        rowsBox.getChildren().add(row);

        applyCss();
        layout();
        debounce.playFromStart();
    }

    private void pushUpdate() {
        if (onChange == null) return;

        List<PlotEquation> eqs = new ArrayList<>();

        for (var n : rowsBox.getChildren()) {
            if (!(n instanceof HBox row)) continue;

            ColorPicker picker = null;
            TextField tf = null;

            for (var c : row.getChildren()) {
                if (c instanceof ColorPicker) picker = (ColorPicker) c;
                if (c instanceof TextField) tf = (TextField) c;
            }

            if (tf == null || picker == null) continue;
            String text = tf.getText() == null ? "" : tf.getText().trim();
            if (text.isBlank()) continue;

            eqs.add(new PlotEquation(text, picker.getValue()));
        }

        onChange.accept(eqs);
    }

    private Color randomColor() {
        Color[] palette = new Color[]{
                Color.web("#2563eb"), // blue
                Color.web("#16a34a"), // green
                Color.web("#dc2626"), // red
                Color.web("#7c3aed"), // purple
                Color.web("#ea580c"), // orange
                Color.web("#0891b2")  // cyan
        };
        int idx = (int) (Math.random() * palette.length);
        return palette[idx];
    }
}
