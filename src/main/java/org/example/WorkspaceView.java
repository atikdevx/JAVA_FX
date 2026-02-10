package com.equationplotter.ui;
import com.equationplotter.ui.GraphCanvas;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;


import javafx.scene.layout.StackPane;

public class WorkspaceView extends StackPane {

    public WorkspaceView() {
        GraphCanvas graph = new GraphCanvas();

        // graph যেন পুরো workspace fill করে
        graph.widthProperty().bind(widthProperty());
        graph.heightProperty().bind(heightProperty());

        getChildren().add(graph);
    }
}
