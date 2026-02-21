package com.equationplotter.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

public class HomeView extends StackPane {

    public HomeView(Runnable onStartPlotting) {
        Image bgImage = new Image(
                getClass().getResource("/images/menu.jpg").toExternalForm()
        );
        // window size adjust
        BackgroundSize size = new BackgroundSize(
                BackgroundSize.AUTO,   // width
                BackgroundSize.AUTO,   // height
                false,                 // widthAsPercentage
                false,                 // heightAsPercentage
                false,                 // contain
                true                   // cover (এটি true থাকলে ছবি চ্যাপ্টা হবে না)
        );
        BackgroundImage bg = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                size
        );

        setBackground(new Background(bg));

        //Only Start button (remove manual title/subtitle/extra texts)
        Button startBtn = new Button("Start Plotting");
        startBtn.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 14;" +
                        "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 700;"
        );

        startBtn.setOnAction(e -> onStartPlotting.run());
        StackPane.setAlignment(startBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(startBtn, new Insets(0, 0, 40, 0)); // little down
        getChildren().setAll(startBtn);
        setPadding(new Insets(40));
    }
}