package com.equationplotter.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Consumer;

public class DraggablePolarEquationPanel extends VBox {
    private double dragOffsetX, dragOffsetY;
    private final VBox rowsBox = new VBox(10);
    private final Button addBtn = new Button("+ Add polar equation");
    private final HBox header = new HBox(10);
    private final Button minBtn = new Button("▾");
    private boolean minimized = false;
    private static final double EXPANDED_W = 420;
    private static final double MIN_W = 180;
    private final Consumer<List<PlotPolarEquation>> onChange;
    private final Map<VBox, PlotPolarEquation> eqByRow = new HashMap<>();

    public DraggablePolarEquationPanel(Consumer<List<PlotPolarEquation>> onChange) {
        this.onChange = onChange;
        setPrefWidth(EXPANDED_W); setSpacing(10); setPadding(new Insets(12));
        setStyleExpanded();

        header.setAlignment(Pos.CENTER_LEFT); header.setPadding(new Insets(9, 12, 9, 12));
        header.setCursor(Cursor.MOVE);
        Label title = new Label("Polar Equations r(t)");
        title.setStyle("-fx-font-weight: 700; -fx-font-size: 14px;");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, minBtn);

        header.setOnMousePressed(e -> { dragOffsetX = e.getX(); dragOffsetY = e.getY(); });
        header.setOnMouseDragged(e -> relocate(getLayoutX() + (e.getX() - dragOffsetX), getLayoutY() + (e.getY() - dragOffsetY)));

        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addRow("", Color.BLUE, true)); // New equations are visible by default
        minBtn.setOnAction(e -> toggleMinimize());

        getChildren().addAll(header, addBtn, rowsBox);

        // Default polar flower, hidden by default (passed false)
        addRow("exp(sin(t)) - 2 * cos(4 * t) + (sin((2 * t - pi) / 24))^5", Color.MAGENTA, false);
        pushUpdate();
    }

    private void toggleMinimize() {
        minimized = !minimized;
        if (minimized) {
            minBtn.setText("▸"); getChildren().setAll(header); setPrefWidth(MIN_W);
        } else {
            minBtn.setText("▾"); getChildren().setAll(header, addBtn, rowsBox); setPrefWidth(EXPANDED_W);
        }
    }

    private void setStyleExpanded() {
        setStyle("-fx-background-color: rgba(255,255,255,0.92); -fx-background-radius: 14; -fx-border-color: rgba(0,0,0,0.10); -fx-border-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0.2, 0, 6);");
    }

    private void addRow(String text, Color color, boolean isVisible) {
        VBox wrap = new VBox(6);
        HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);

        ColorPicker picker = new ColorPicker(color);
        picker.setStyle("-fx-color-label-visible: false; -fx-pref-width: 40px;");

        TextField tf = new TextField(text);
        tf.setPromptText("1 - sin(t)");
        HBox.setHgrow(tf, Priority.ALWAYS);

        Button eye = new Button(isVisible ? "👁" : "🚫");
        eye.setOpacity(isVisible ? 1.0 : 0.6);

        Button del = new Button("✕");
        row.getChildren().addAll(picker, tf, eye, del);

        VBox paramsBox = new VBox(6);
        wrap.getChildren().addAll(row, paramsBox);
        rowsBox.getChildren().add(wrap);

        PlotPolarEquation eq = new PlotPolarEquation(tf.getText(), picker.getValue());
        eq.setVisible(isVisible); // Apply the hidden state
        eqByRow.put(wrap, eq);

        rebuildParamsUI(paramsBox, eq);

        tf.textProperty().addListener((o,a,b)->{ eq.setRawText(b); rebuildParamsUI(paramsBox, eq); pushUpdate(); });
        picker.valueProperty().addListener((o,a,b)->{ eq.setColor(b); pushUpdate(); });

        eye.setOnAction(e -> {
            eq.setVisible(!eq.isVisible());
            eye.setText(eq.isVisible() ? "👁" : "🚫");
            eye.setOpacity(eq.isVisible() ? 1 : 0.6);
            pushUpdate();
        });

        del.setOnAction(e->{ eqByRow.remove(wrap); rowsBox.getChildren().remove(wrap); pushUpdate(); });

        pushUpdate();
    }

    private void rebuildParamsUI(VBox box, PlotPolarEquation eq) {
        box.getChildren().clear();
        for (String p : eq.getParamNames()) box.getChildren().add(buildParamRow(eq, p));
    }

    private HBox buildParamRow(PlotPolarEquation eq, String name) {
        Label lbl = new Label(name + " =");
        TextField minField = new TextField("-10"); minField.setPrefWidth(45);
        TextField maxField = new TextField("10"); maxField.setPrefWidth(45);
        Slider s = new Slider(-10, 10, eq.getParam(name, 1)); s.setPrefWidth(160);
        Label val = new Label(String.format("%.2f", s.getValue())); val.setMinWidth(50);

        Runnable updateMin = () -> {
            try {
                double newMin = Double.parseDouble(minField.getText());
                s.setMin(newMin);
                if (s.getValue() < newMin) s.setValue(newMin);
            } catch (NumberFormatException ex) {
                minField.setText(String.valueOf(s.getMin()));
            }
        };

        Runnable updateMax = () -> {
            try {
                double newMax = Double.parseDouble(maxField.getText());
                s.setMax(newMax);
                if (s.getValue() > newMax) s.setValue(newMax);
            } catch (NumberFormatException ex) {
                maxField.setText(String.valueOf(s.getMax()));
            }
        };

        minField.setOnAction(e -> updateMin.run());
        maxField.setOnAction(e -> updateMax.run());

        minField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) updateMin.run();
        });
        maxField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) updateMax.run();
        });

        Button play = new Button("▶");
        final double[] dir = {1};
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            double v = s.getValue(), min = s.getMin(), max = s.getMax();
            double nv = v + (max - min) * 0.001 * dir[0];
            if (nv >= max) { nv = max; dir[0] = -1; } else if (nv <= min) { nv = min; dir[0] = 1; }
            s.setValue(nv);
        }));
        tl.setCycleCount(Animation.INDEFINITE);
        play.setOnAction(e -> { if (tl.getStatus() == Animation.Status.RUNNING) { tl.stop(); play.setText("▶"); } else { tl.play(); play.setText("⏸"); } });

        s.valueProperty().addListener((o,a,b)->{ val.setText(String.format("%.2f", b.doubleValue())); eq.setParam(name, b.doubleValue()); pushUpdate(); });
        HBox rangeBox = new HBox(3, minField, new Label("…"), maxField);
        HBox row = new HBox(6, lbl, rangeBox, s, val, play);
        row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(4));
        row.setBackground(new Background(new BackgroundFill(Color.rgb(0,0,0,0.04), new CornerRadii(8), Insets.EMPTY)));
        return row;
    }

    private void pushUpdate() {
        if (onChange == null) return;
        List<PlotPolarEquation> list = new ArrayList<>();
        for (PlotPolarEquation e : eqByRow.values()) if (e.getRawText() != null && !e.getRawText().isBlank()) list.add(e);
        onChange.accept(list);
    }
}