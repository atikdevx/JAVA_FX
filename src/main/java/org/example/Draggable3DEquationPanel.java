package com.equationplotter.ui;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Draggable3DEquationPanel extends VBox {

    private final VBox rowsBox = new VBox(10);
    private final Button addBtn = new Button("+ Add 3D graph");
    private final Map<VBox, Plot3DDefinition> plotByRow = new LinkedHashMap<>();
    private final Consumer<List<Plot3DDefinition>> onChange;

    private double dragOffsetX;
    private double dragOffsetY;
    private boolean minimized = false;

    private VBox hintBox;
    private HBox header;

    public Draggable3DEquationPanel(Consumer<List<Plot3DDefinition>> onChange) {
        this.onChange = onChange;

        setPrefWidth(460);
        setSpacing(10);
        setPadding(new Insets(14));
        setStyle("""
            -fx-background-color: rgba(255,255,255,0.82);
            -fx-background-radius: 18;
            -fx-border-color: rgba(220,220,220,0.85);
            -fx-border-radius: 18;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 24, 0.2, 0, 8);
        """);

        buildHeader();

        hintBox = createHintBox();

        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle("""
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-background-color: #dfe6f7;
            -fx-text-fill: #24314d;
            -fx-background-radius: 10;
            -fx-padding: 10 12;
            -fx-cursor: hand;
        """);
        addBtn.setOnAction(e -> addRow("x^2+y^2", Color.HOTPINK));

        getChildren().addAll(header, hintBox, addBtn, rowsBox);

        pushUpdate();
    }

    private void buildHeader() {
        header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(8, 10, 8, 10));
        header.setCursor(Cursor.MOVE);

        Label title = new Label("3D Graphs");
        title.setStyle("-fx-font-weight: 800; -fx-font-size: 20px; -fx-text-fill: #1b2232;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minBtn = new Button("▾");
        minBtn.setStyle("""
            -fx-background-color: #eef2ff;
            -fx-text-fill: #24314d;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """);

        minBtn.setOnAction(e -> {
            minimized = !minimized;

            if (minimized) {
                minBtn.setText("▸");
                getChildren().setAll(header);
                setPrefWidth(220);
            } else {
                minBtn.setText("▾");
                getChildren().setAll(header, hintBox, addBtn, rowsBox);
                setPrefWidth(460);
            }
        });

        header.getChildren().addAll(title, spacer, minBtn);

        header.setOnMousePressed(e -> {
            dragOffsetX = e.getX();
            dragOffsetY = e.getY();
        });

        header.setOnMouseDragged(e ->
                relocate(getLayoutX() + (e.getX() - dragOffsetX), getLayoutY() + (e.getY() - dragOffsetY))
        );
    }

    private VBox createHintBox() {
        VBox hints = new VBox(4);
        hints.setPadding(new Insets(10));
        hints.setBackground(new Background(
                new BackgroundFill(Color.rgb(20, 30, 60, 0.05), new CornerRadii(10), Insets.EMPTY)
        ));

        Label h1 = new Label("Surface: z = sin(x) + cos(y)");
        Label h2 = new Label("Implicit: x^2+y^2+z^2 = 25");
        Label h3 = new Label("Curve: x=cos(t); y=sin(t); z=t");

        h1.setStyle("-fx-font-size: 12px; -fx-text-fill: #34415f;");
        h2.setStyle("-fx-font-size: 12px; -fx-text-fill: #34415f;");
        h3.setStyle("-fx-font-size: 12px; -fx-text-fill: #34415f;");

        hints.getChildren().addAll(h1, h2, h3);
        return hints;
    }

    private void addRow(String text, Color color) {
        VBox wrap = new VBox(6);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        ColorPicker picker = new ColorPicker(color);
        picker.setStyle("-fx-color-label-visible: false; -fx-pref-width: 44px;");

        TextArea input = new TextArea(text);
        input.setWrapText(true);
        input.setPrefRowCount(2);
        input.setPromptText("z=f(x,y)  or  f(x,y,z)=0  or  x=...; y=...; z=...");
        input.setStyle("""
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-font-size: 14px;
        """);
        HBox.setHgrow(input, Priority.ALWAYS);

        Button eye = new Button("Hide");
        eye.setTooltip(new Tooltip("Show / Hide graph"));
        eye.setMinWidth(52);
        eye.setStyle("""
            -fx-background-color: #eef2ff;
            -fx-text-fill: #2f3b59;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """);

        Button del = new Button("X");
        del.setTooltip(new Tooltip("Delete"));
        del.setMinWidth(38);
        del.setStyle("""
            -fx-background-color: #fff1f1;
            -fx-text-fill: #c93c3c;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """);

        top.getChildren().addAll(picker, input, eye, del);
        wrap.getChildren().add(top);
        rowsBox.getChildren().add(wrap);

        Plot3DDefinition def = new Plot3DDefinition(text, color);
        plotByRow.put(wrap, def);

        input.textProperty().addListener((o, oldVal, newVal) -> {
            def.setRawText(newVal);
            pushUpdate();
        });

        picker.valueProperty().addListener((o, oldVal, newVal) -> {
            def.setColor(newVal);
            pushUpdate();
        });

        eye.setOnAction(e -> {
            def.setVisible(!def.isVisible());
            eye.setText(def.isVisible() ? "Hide" : "Show");
            eye.setOpacity(def.isVisible() ? 1.0 : 0.75);
            pushUpdate();
        });

        del.setOnAction(e -> {
            plotByRow.remove(wrap);
            rowsBox.getChildren().remove(wrap);
            pushUpdate();
        });
    }

    private void pushUpdate() {
        if (onChange == null) return;

        List<Plot3DDefinition> list = new ArrayList<>();
        for (Plot3DDefinition def : plotByRow.values()) {
            if (def.getRawText() != null && !def.getRawText().isBlank()) {
                list.add(def);
            }
        }
        onChange.accept(list);
    }
}