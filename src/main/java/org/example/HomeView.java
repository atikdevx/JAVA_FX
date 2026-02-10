package com.equationplotter.ui;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class HomeView extends StackPane {

    public HomeView(Runnable onStartPlotting) {

        // --- Background (math vibe) ---
        // --- Background image ---
        Image bgImage = new Image(
                getClass().getResource("/images/menu_bg.jpg").toExternalForm()
        );

        BackgroundSize size = new BackgroundSize(
                100, 100, true, true, true, false
        );

        BackgroundImage bg = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                size
        );

        setBackground(new Background(bg));


        // --- Title ---
        Label title = new Label("Pika Plotter");
        title.setFont(Font.font(38));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Plot • Transform • Differentiate • Integrate");
        subtitle.setFont(Font.font(16));
        subtitle.setTextFill(Color.web("#b9c2ff"));

        // --- Start Button ---
        Button startBtn = new Button("Start Plotting");
        startBtn.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 14;" +
                        "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;"
        );

        // hover effect
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 14;" +
                        "-fx-background-color: #2563eb;" +
                        "-fx-text-fill: white;"
        ));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 14;" +
                        "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;"
        ));

        startBtn.setOnAction(e -> onStartPlotting.run());

        // --- Layout ---
        VBox box = new VBox(10, title, subtitle, startBtn);
        box.setAlignment(Pos.CENTER);

        getChildren().add(box);
        setPadding(new Insets(40));
    }
}
