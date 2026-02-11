package com.equationplotter.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Consumer;

public class DraggableEquationPanel extends VBox {

    private double dragOffsetX, dragOffsetY;

    private final VBox rowsBox = new VBox(10);
    private final Button addBtn = new Button("+ Add equation");

    private final HBox header = new HBox(10);
    private final Button minBtn = new Button("▾");
    private boolean minimized = false;

    private static final double EXPANDED_W = 420;
    private static final double MIN_W = 180;

    private final Consumer<List<PlotEquation>> onChange;
    private final PauseTransition debounce = new PauseTransition(Duration.millis(180));

    // row -> eq state preserve
    private final Map<VBox, PlotEquation> eqByRow = new HashMap<>();
    private final Map<String, Double> globalDefaults = new HashMap<>(); // remember last param values

    public DraggableEquationPanel(Consumer<List<PlotEquation>> onChange) {
        this.onChange = onChange;

        setFillWidth(true);
        setPrefWidth(EXPANDED_W);
        setMinWidth(EXPANDED_W);
        setMaxWidth(EXPANDED_W);

        setSpacing(10);
        setPadding(new Insets(12));
        setStyleExpanded();

        // header
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

        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle("-fx-padding: 10 12; -fx-background-radius: 10;");
        addBtn.setOnAction(e -> addRow("", randomColor()));

        rowsBox.setFillWidth(true);
        rowsBox.setMaxWidth(Double.MAX_VALUE);
        rowsBox.setPadding(new Insets(2));

        debounce.setOnFinished(e -> pushUpdate());

        minBtn.setOnAction(e -> toggleMinimize());

        getChildren().setAll(header, addBtn, rowsBox);

        addRow("y=sin(x)", Color.web("#2563eb"));
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

    // ---------------- Row UI (VBox = main row + params box) ----------------
    private void addRow(String initialText, Color initialColor) {

        VBox rowWrap = new VBox(8);
        rowWrap.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        ColorPicker picker = new ColorPicker(initialColor != null ? initialColor : randomColor());
        picker.setPrefWidth(55);

        TextField tf = new TextField(initialText != null ? initialText : "");
        tf.setPromptText("y=... or x^2+y^2=1 or y^2=4*x or x*y=1");
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setPrefHeight(36);
        tf.setStyle("-fx-background-radius: 10; -fx-padding: 9 10;");
        HBox.setHgrow(tf, Priority.ALWAYS);

        Button eye = new Button("👁");
        eye.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-padding: 6 10;" +
                        "-fx-background-color: rgba(0,0,0,0.06);"
        );

        Button del = new Button("✕");
        del.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-padding: 6 10;" +
                        "-fx-background-color: rgba(239,68,68,0.12);" +
                        "-fx-text-fill: #b91c1c;"
        );

        row.getChildren().addAll(picker, tf, eye, del);

        // params area under row
        VBox paramsBox = new VBox(8);
        paramsBox.setPadding(new Insets(0, 0, 0, 8));

        rowWrap.getChildren().addAll(row, paramsBox);
        rowsBox.getChildren().add(rowWrap);

        // create equation state
        PlotEquation eq = new PlotEquation(tf.getText(), picker.getValue());
        // apply remembered defaults
        for (String p : eq.getParamNames()) {
            double dv = globalDefaults.getOrDefault(p, 1.0);
            eq.setParam(p, dv);
        }
        eqByRow.put(rowWrap, eq);

        // build sliders initially
        rebuildParamsUI(paramsBox, eq);

        // input change -> rebuild sliders + update
        tf.textProperty().addListener((o, a, b) -> {
            PlotEquation current = eqByRow.get(rowWrap);
            if (current == null) return;

            current.setRawText(b);
            rebuildParamsUI(paramsBox, current);
            debounce.playFromStart();
        });

        picker.valueProperty().addListener((o, a, b) -> {
            PlotEquation current = eqByRow.get(rowWrap);
            if (current == null) return;

            current.setColor(b);
            debounce.playFromStart();
        });

        eye.setOnAction(e -> {
            PlotEquation current = eqByRow.get(rowWrap);
            if (current == null) return;

            current.setVisible(!current.isVisible());
            if (current.isVisible()) {
                eye.setText("👁");
                eye.setOpacity(1.0);
            } else {
                eye.setText("🚫");
                eye.setOpacity(0.6);
            }
            pushUpdate();
        });

        del.setOnAction(e -> {
            eqByRow.remove(rowWrap);
            rowsBox.getChildren().remove(rowWrap);
            pushUpdate();
        });

        applyCss();
        layout();
        debounce.playFromStart();
    }

    // rebuild sliders UI under a row
    private void rebuildParamsUI(VBox paramsBox, PlotEquation eq) {
        paramsBox.getChildren().clear();

        List<String> ps = eq.getParamNames();
        if (ps == null || ps.isEmpty()) return;

        for (String p : ps) {
            paramsBox.getChildren().add(buildParamRow(eq, p));
        }
    }

    private HBox buildParamRow(PlotEquation eq, String name) {

        Label lbl = new Label(name + " = ");
        lbl.setMinWidth(38);
        lbl.setStyle("-fx-font-weight: 700; -fx-text-fill: #111827;");

        Slider s = new Slider(-10, 10, eq.getParam(name, globalDefaults.getOrDefault(name, 1.0)));
        s.setPrefWidth(230);

        Label val = new Label(String.format("%.2f", s.getValue()));
        val.setMinWidth(56);

        Button play = new Button("▶");
        play.setStyle("-fx-background-radius: 10; -fx-padding: 4 10;");

        // direction data: +1 or -1
        play.setUserData(1.0);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(30), e -> {
                    double v = s.getValue();
                    double dir = (play.getUserData() instanceof Number n) ? n.doubleValue() : 1.0;

                    double nv = v + 0.08 * dir;

                    if (nv > 10) { nv = 10; play.setUserData(-1.0); }
                    if (nv < -10){ nv = -10; play.setUserData( 1.0); }

                    s.setValue(nv);
                })
        );
        tl.setCycleCount(Animation.INDEFINITE);

        play.setOnAction(e -> {
            if (tl.getStatus() == Animation.Status.RUNNING) {
                tl.stop();
                play.setText("▶");
            } else {
                tl.play();
                play.setText("⏸");
            }
        });

        // slider change -> update param + remember + redraw
        s.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            val.setText(String.format("%.2f", v));

            eq.setParam(name, v);
            globalDefaults.put(name, v);

            debounce.playFromStart();
        });

        HBox row = new HBox(10, lbl, s, val, play);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void pushUpdate() {
        if (onChange == null) return;

        List<PlotEquation> eqs = new ArrayList<>();
        for (PlotEquation eq : eqByRow.values()) {
            if (eq == null) continue;
            String t = eq.getRawText() == null ? "" : eq.getRawText().trim();
            if (!t.isBlank()) eqs.add(eq);
        }
        onChange.accept(eqs);
    }

    private Color randomColor() {
        Color[] palette = new Color[]{
                Color.web("#2563eb"),
                Color.web("#16a34a"),
                Color.web("#dc2626"),
                Color.web("#7c3aed"),
                Color.web("#ea580c"),
                Color.web("#0891b2")
        };
        int idx = (int) (Math.random() * palette.length);
        return palette[idx];
    }
}
