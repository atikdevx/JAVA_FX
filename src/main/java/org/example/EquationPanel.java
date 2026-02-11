package com.equationplotter.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class EquationPanel extends VBox {

    private final VBox equationList = new VBox(8);

    public EquationPanel() {

        setPrefWidth(260);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: #f7f7f7;");

        // ➕ Add equation button
        Button addBtn = new Button("+ Add Equation");
        addBtn.setMaxWidth(Double.MAX_VALUE);

        addBtn.setOnAction(e -> addEquationField());

        getChildren().addAll(addBtn, equationList);

        // start with one field
        addEquationField();
    }

    private void addEquationField() {
        TextField field = new TextField();
        field.setPromptText("y = ...");

        equationList.getChildren().add(field);
    }
}
