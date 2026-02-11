package com.equationplotter.ui;

import javafx.application.Platform;
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

        DraggableEquationPanel overlay = new DraggableEquationPanel();
        overlay.setManaged(false);

        getChildren().addAll(graph, overlay);
        Platform.runLater(() -> {
            graph.setWidth(getWidth());
            graph.setHeight(getHeight());
            graph.draw();

            overlay.applyCss();
            overlay.autosize();
            overlay.relocate(20, 70);
        });
    }
}
