package com.equationplotter;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        Label title = new Label("Equation Plotter (JavaFX) - Window Ready");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        root.setTop(title);
        BorderPane.setMargin(title, new Insets(20));

        Scene scene = new Scene(root, 1000, 650);

        stage.setTitle("Pika Plotter handle2");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
