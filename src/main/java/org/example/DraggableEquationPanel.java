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

    // row -> equation map
    private final Map<VBox, PlotEquation> eqByRow = new HashMap<>();

    public DraggableEquationPanel(Consumer<List<PlotEquation>> onChange) {
        this.onChange = onChange;

        setPrefWidth(EXPANDED_W);
        setSpacing(10);
        setPadding(new Insets(12));
        setStyleExpanded();

        // ===== Header =====
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(9, 12, 9, 12));
        header.setCursor(Cursor.MOVE);

        Label title = new Label("Equations");
        title.setStyle("-fx-font-weight: 700; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, minBtn);

        // drag support
        header.setOnMousePressed(e -> {
            dragOffsetX = e.getX();
            dragOffsetY = e.getY();
        });

        header.setOnMouseDragged(e ->
                relocate(
                        getLayoutX() + (e.getX() - dragOffsetX),
                        getLayoutY() + (e.getY() - dragOffsetY)
                )
        );

        // ===== Add button =====
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addRow("", randomColor()));

        minBtn.setOnAction(e -> toggleMinimize());

        getChildren().addAll(header, addBtn, rowsBox);

        addRow("y=sin(x)", Color.BLUE);
        pushUpdate();
    }

    // ================= MINIMIZE =================
    private void toggleMinimize() {
        minimized = !minimized;

        if (minimized) {
            minBtn.setText("▸");
            getChildren().setAll(header);
            setPrefWidth(MIN_W);
        } else {
            minBtn.setText("▾");
            getChildren().setAll(header, addBtn, rowsBox);
            setPrefWidth(EXPANDED_W);
        }
    }

    private void setStyleExpanded() {
        setStyle("""
            -fx-background-color: rgba(255,255,255,0.92);
            -fx-background-radius: 14;
            -fx-border-color: rgba(0,0,0,0.10);
            -fx-border-radius: 14;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0.2, 0, 6);
        """);
    }

    // ================= ADD ROW =================
    private void addRow(String text, Color color) {

        VBox wrap = new VBox(6);

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        ColorPicker picker = new ColorPicker(color);

        TextField tf = new TextField(text);
        tf.setPromptText("y=mx+c or sin(ax)");
        HBox.setHgrow(tf, Priority.ALWAYS);

        // 👁 SHOW / HIDE BUTTON
        Button eye = new Button("👁");

        // ❌ DELETE BUTTON
        Button del = new Button("✕");

        row.getChildren().addAll(picker, tf, eye, del);

        VBox paramsBox = new VBox(6);
        wrap.getChildren().addAll(row, paramsBox);

        rowsBox.getChildren().add(wrap);

        PlotEquation eq = new PlotEquation(tf.getText(), picker.getValue());
        eqByRow.put(wrap, eq);

        rebuildParamsUI(paramsBox, eq);

        // ===== listeners =====
        tf.textProperty().addListener((o,a,b)->{
            eq.setRawText(b);
            rebuildParamsUI(paramsBox, eq);
            pushUpdate();
        });

        picker.valueProperty().addListener((o,a,b)->{
            eq.setColor(b);
            pushUpdate();
        });

        // 👁 hide/show logic
        eye.setOnAction(e -> {
            eq.setVisible(!eq.isVisible());

            if (eq.isVisible()) {
                eye.setText("👁");
                eye.setOpacity(1);
            } else {
                eye.setText("🚫");
                eye.setOpacity(0.6);
            }

            pushUpdate(); // instant redraw
        });

        // ❌ delete
        del.setOnAction(e->{
            eqByRow.remove(wrap);
            rowsBox.getChildren().remove(wrap);
            pushUpdate();
        });

        pushUpdate();
    }

    // ================= PARAM UI =================
    private void rebuildParamsUI(VBox box, PlotEquation eq) {
        box.getChildren().clear();

        for (String p : eq.getParamNames()) {
            box.getChildren().add(buildParamRow(eq, p));
        }
    }

    // ================= PARAM ROW =================
    private HBox buildParamRow(PlotEquation eq, String name) {

        Label lbl = new Label(name + " =");

        TextField minField = new TextField("-10");
        TextField maxField = new TextField("10");
        minField.setPrefWidth(45);
        maxField.setPrefWidth(45);

        Slider s = new Slider(-10, 10, eq.getParam(name, 1));
        s.setPrefWidth(160);

        Label val = new Label(String.format("%.2f", s.getValue()));
        val.setMinWidth(50);

        // ▶ PLAY BUTTON
        Button play = new Button("▶");

        final double[] dir = {1};

        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(16), e -> {

                    double v = s.getValue();
                    double min = s.getMin();
                    double max = s.getMax();

                    double nv = v + 0.08 * dir[0];

                    // 🔁 infinite back-and-forth
                    if (nv >= max) { nv = max; dir[0] = -1; }
                    else if (nv <= min) { nv = min; dir[0] = 1; }

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

        // ⚡ instant redraw on slider change
        s.valueProperty().addListener((o,a,b)->{
            double v = b.doubleValue();
            val.setText(String.format("%.2f", v));
            eq.setParam(name, v);
            pushUpdate();
        });

        // manual min/max update
        Runnable updateRange = () -> {
            try {
                double min = Double.parseDouble(minField.getText());
                double max = Double.parseDouble(maxField.getText());
                if (min >= max) return;

                s.setMin(min);
                s.setMax(max);
            } catch (Exception ignored) {}
        };

        minField.setOnAction(e -> updateRange.run());
        maxField.setOnAction(e -> updateRange.run());

        HBox rangeBox = new HBox(3, minField, new Label("…"), maxField);

        HBox row = new HBox(6, lbl, rangeBox, s, val, play);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4));
        row.setBackground(new Background(new BackgroundFill(
                Color.rgb(0,0,0,0.04),
                new CornerRadii(8),
                Insets.EMPTY
        )));

        return row;
    }

    // ================= PUSH UPDATE =================
    private void pushUpdate() {
        if (onChange == null) return;

        List<PlotEquation> list = new ArrayList<>();
        for (PlotEquation e : eqByRow.values()) {
            if (e.getRawText() != null && !e.getRawText().isBlank())
                list.add(e);
        }

        onChange.accept(list);
    }

    // ================= RANDOM COLOR =================
    private Color randomColor() {
        Color[] c = {
                Color.BLUE, Color.RED, Color.GREEN,
                Color.ORANGE, Color.PURPLE
        };
        return c[(int)(Math.random()*c.length)];
    }
}

//package com.equationplotter.ui;
//
//import javafx.animation.Animation;
//import javafx.animation.KeyFrame;
//import javafx.animation.Timeline;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Cursor;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.util.Duration;
//
//import java.util.*;
//import java.util.function.Consumer;
//
//public class DraggableEquationPanel extends VBox {
//
//    private double dragOffsetX, dragOffsetY;
//
//    private final VBox rowsBox = new VBox(10);
//    private final Button addBtn = new Button("+ Add equation");
//
//    private final HBox header = new HBox(10);
//    private final Button minBtn = new Button("▾");
//    private boolean minimized = false;
//
//    private static final double EXPANDED_W = 420;
//    private static final double MIN_W = 180;
//
//    private final Consumer<List<PlotEquation>> onChange;
//
//    // row -> eq
//    private final Map<VBox, PlotEquation> eqByRow = new HashMap<>();
//
//    public DraggableEquationPanel(Consumer<List<PlotEquation>> onChange) {
//        this.onChange = onChange;
//
//        setPrefWidth(EXPANDED_W);
//        setSpacing(10);
//        setPadding(new Insets(12));
//        setStyleExpanded();
//
//        // ===== Header =====
//        header.setAlignment(Pos.CENTER_LEFT);
//        header.setPadding(new Insets(9, 12, 9, 12));
//        header.setCursor(Cursor.MOVE);
//
//        Label title = new Label("Equations");
//        title.setStyle("-fx-font-weight: 700; -fx-font-size: 14px;");
//
//        Region spacer = new Region();
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//
//        header.getChildren().addAll(title, spacer, minBtn);
//
//        // drag
//        header.setOnMousePressed(e -> {
//            dragOffsetX = e.getX();
//            dragOffsetY = e.getY();
//        });
//
//        header.setOnMouseDragged(e -> {
//            relocate(
//                    getLayoutX() + (e.getX() - dragOffsetX),
//                    getLayoutY() + (e.getY() - dragOffsetY)
//            );
//        });
//
//        // ===== Add button =====
//        addBtn.setMaxWidth(Double.MAX_VALUE);
//        addBtn.setOnAction(e -> addRow("", randomColor()));
//
//        minBtn.setOnAction(e -> toggleMinimize());
//
//        getChildren().addAll(header, addBtn, rowsBox);
//
//        addRow("y=sin(x)", Color.BLUE);
//        pushUpdate();
//    }
//
//    // ================= MINIMIZE =================
//    private void toggleMinimize() {
//        minimized = !minimized;
//
//        if (minimized) {
//            minBtn.setText("▸");
//            getChildren().setAll(header);
//            setPrefWidth(MIN_W);
//        } else {
//            minBtn.setText("▾");
//            getChildren().setAll(header, addBtn, rowsBox);
//            setPrefWidth(EXPANDED_W);
//        }
//    }
//
//    private void setStyleExpanded() {
//        setStyle("""
//            -fx-background-color: rgba(255,255,255,0.92);
//            -fx-background-radius: 14;
//            -fx-border-color: rgba(0,0,0,0.10);
//            -fx-border-radius: 14;
//            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0.2, 0, 6);
//        """);
//    }
//
//    // ================= ADD ROW =================
//    private void addRow(String text, Color color) {
//
//        VBox wrap = new VBox(6);
//
//        HBox row = new HBox(8);
//        row.setAlignment(Pos.CENTER_LEFT);
//
//        ColorPicker picker = new ColorPicker(color);
//
//        TextField tf = new TextField(text);
//        tf.setPromptText("y=mx+c or sin(ax)");
//        HBox.setHgrow(tf, Priority.ALWAYS);
//
//        Button del = new Button("✕");
//
//        row.getChildren().addAll(picker, tf, del);
//
//        VBox paramsBox = new VBox(6);
//        wrap.getChildren().addAll(row, paramsBox);
//
//        rowsBox.getChildren().add(wrap);
//
//        PlotEquation eq = new PlotEquation(tf.getText(), picker.getValue());
//        eqByRow.put(wrap, eq);
//
//        rebuildParamsUI(paramsBox, eq);
//
//        // ===== listeners =====
//        tf.textProperty().addListener((o,a,b)->{
//            eq.setRawText(b);
//            rebuildParamsUI(paramsBox, eq);
//            pushUpdate(); // 🔥 instant redraw
//        });
//
//        picker.valueProperty().addListener((o,a,b)->{
//            eq.setColor(b);
//            pushUpdate(); // 🔥 instant redraw
//        });
//
//        del.setOnAction(e->{
//            eqByRow.remove(wrap);
//            rowsBox.getChildren().remove(wrap);
//            pushUpdate();
//        });
//
//        pushUpdate();
//    }
//
//    // ================= PARAM UI =================
//    private void rebuildParamsUI(VBox box, PlotEquation eq) {
//        box.getChildren().clear();
//
//        for (String p : eq.getParamNames()) {
//            box.getChildren().add(buildParamRow(eq, p));
//        }
//    }
//
//    // ================= PARAM ROW (IMPORTANT) =================
//    private HBox buildParamRow(PlotEquation eq, String name) {
//
//        Label lbl = new Label(name + " =");
//
//        TextField minField = new TextField("-10");
//        TextField maxField = new TextField("10");
//        minField.setPrefWidth(45);
//        maxField.setPrefWidth(45);
//
//        Slider s = new Slider(-10, 10, eq.getParam(name, 1));
//        s.setPrefWidth(160);
//
//        Label val = new Label(String.format("%.2f", s.getValue()));
//        val.setMinWidth(50);
//
//        Button play = new Button("▶");
//
//        // direction holder
//        final double[] dir = {1};
//
//        Timeline tl = new Timeline(
//                new KeyFrame(Duration.millis(16), e -> {
//
//                    double v = s.getValue();
//                    double min = s.getMin();
//                    double max = s.getMax();
//
//                    double nv = v + 0.08 * dir[0];
//
//                    // 🔁 back-and-forth animation
//                    if (nv >= max) { nv = max; dir[0] = -1; }
//                    else if (nv <= min) { nv = min; dir[0] = 1; }
//
//                    s.setValue(nv);
//                })
//        );
//        tl.setCycleCount(Animation.INDEFINITE);
//
//        play.setOnAction(e -> {
//            if (tl.getStatus() == Animation.Status.RUNNING) {
//                tl.stop();
//                play.setText("▶");
//            } else {
//                tl.play();
//                play.setText("⏸");
//            }
//        });
//
//        // 🔥 instant redraw on slider move
//        s.valueProperty().addListener((o,a,b)->{
//            double v = b.doubleValue();
//            val.setText(String.format("%.2f", v));
//            eq.setParam(name, v);
//            pushUpdate(); // ⚡ real-time animation
//        });
//
//        // ===== manual range change =====
//        Runnable updateRange = () -> {
//            try {
//                double min = Double.parseDouble(minField.getText());
//                double max = Double.parseDouble(maxField.getText());
//                if (min >= max) return;
//
//                s.setMin(min);
//                s.setMax(max);
//            } catch (Exception ignored) {}
//        };
//
//        minField.setOnAction(e -> updateRange.run());
//        maxField.setOnAction(e -> updateRange.run());
//
//        HBox rangeBox = new HBox(3, minField, new Label("…"), maxField);
//
//        HBox row = new HBox(6, lbl, rangeBox, s, val, play);
//        row.setAlignment(Pos.CENTER_LEFT);
//        row.setPadding(new Insets(4));
//        row.setBackground(new Background(new BackgroundFill(
//                Color.rgb(0,0,0,0.04),
//                new CornerRadii(8),
//                Insets.EMPTY
//        )));
//
//        return row;
//    }
//
//    // ================= PUSH UPDATE =================
//    private void pushUpdate() {
//        if (onChange == null) return;
//
//        List<PlotEquation> list = new ArrayList<>();
//        for (PlotEquation e : eqByRow.values()) {
//            if (e.getRawText() != null && !e.getRawText().isBlank())
//                list.add(e);
//        }
//
//        onChange.accept(list);
//    }
//
//    // ================= COLOR =================
//    private Color randomColor() {
//        Color[] c = {
//                Color.BLUE, Color.RED, Color.GREEN,
//                Color.ORANGE, Color.PURPLE
//        };
//        return c[(int)(Math.random()*c.length)];
//    }
//}