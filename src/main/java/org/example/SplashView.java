package com.equationplotter.ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class SplashView extends StackPane {

    public SplashView(Runnable onFinished) {

        setBackground(new Background(new BackgroundFill(
                new LinearGradient(
                        0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.00, Color.web("#0f1324")),
                        new Stop(0.25, Color.web("#1a2340")),
                        new Stop(0.55, Color.web("#242650")),
                        new Stop(0.80, Color.web("#2d2147")),
                        new Stop(1.00, Color.web("#14192d"))
                ),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        Region overlay1 = new Region();
        overlay1.setBackground(new Background(new BackgroundFill(
                new LinearGradient(
                        0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(120, 180, 255, 0.08)),
                        new Stop(0.5, Color.rgb(255, 255, 255, 0.02)),
                        new Stop(1, Color.rgb(255, 155, 215, 0.08))
                ),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        Region overlay2 = new Region();
        overlay2.setBackground(new Background(new BackgroundFill(
                new RadialGradient(
                        0, 0, 0.30, 0.35, 0.55, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(160, 200, 255, 0.14)),
                        new Stop(1, Color.rgb(160, 200, 255, 0.0))
                ),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        HBox titleBox = new HBox(0);
        titleBox.setAlignment(Pos.CENTER);

        List<Label> letters = new ArrayList<>();
        String text = "Pika Plotter";

        for (int i = 0; i < text.length(); i++) {
            Label ch = new Label(String.valueOf(text.charAt(i)));
            ch.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 70));
            ch.setOpacity(0);
            ch.setTextFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0.00, Color.web("#f7fbff")),
                    new Stop(0.30, Color.web("#c9ddff")),
                    new Stop(0.65, Color.web("#d9c4ff")),
                    new Stop(1.00, Color.web("#ffe3ef"))
            ));
            letters.add(ch);
            titleBox.getChildren().add(ch);
        }

        Label subtitle = new Label("A modern mathematical visualization space");
        subtitle.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 22));
        subtitle.setTextFill(Color.rgb(228, 234, 247, 0.88));
        subtitle.setOpacity(0);
        subtitle.setTranslateY(14);

        VBox centerBox = new VBox(18, titleBox, subtitle);
        centerBox.setAlignment(Pos.CENTER);

        Region titleGlow = new Region();
        titleGlow.setPrefSize(620, 320);
        titleGlow.setMaxSize(620, 320);
        titleGlow.setBackground(new Background(new BackgroundFill(
                new RadialGradient(
                        0, 0, 0.5, 0.5, 0.55, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(185, 205, 255, 0.15)),
                        new Stop(0.55, Color.rgb(230, 180, 255, 0.08)),
                        new Stop(1, Color.rgb(185, 205, 255, 0.0))
                ),
                new CornerRadii(999),
                Insets.EMPTY
        )));

        getChildren().addAll(overlay1, overlay2, titleGlow, centerBox);

        overlay1.prefWidthProperty().bind(widthProperty());
        overlay1.prefHeightProperty().bind(heightProperty());
        overlay2.prefWidthProperty().bind(widthProperty());
        overlay2.prefHeightProperty().bind(heightProperty());

        playConstructAnimation(letters, titleBox, subtitle, onFinished);
    }

    private void playConstructAnimation(List<Label> letters, Node titleNode, Label subtitle, Runnable onFinished) {
        ParallelTransition construct = new ParallelTransition();

        for (int i = 0; i < letters.size(); i++) {
            Label ch = letters.get(i);

            double startX = 0;
            double startY = 0;

            int direction = i % 4;
            switch (direction) {
                case 0 -> startY = -220;
                case 1 -> startY = 220;
                case 2 -> startX = -280;
                case 3 -> startX = 280;
            }

            ch.setTranslateX(startX);
            ch.setTranslateY(startY);
            ch.setScaleX(0.7);
            ch.setScaleY(0.7);
            ch.setOpacity(0);

            TranslateTransition move = new TranslateTransition(Duration.seconds(1.0), ch);
            move.setToX(0);
            move.setToY(0);
            move.setInterpolator(Interpolator.SPLINE(0.2, 0.85, 0.2, 1));

            FadeTransition fade = new FadeTransition(Duration.seconds(0.9), ch);
            fade.setFromValue(0);
            fade.setToValue(1);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(0.9), ch);
            scale.setToX(1.0);
            scale.setToY(1.0);

            ParallelTransition oneLetter = new ParallelTransition(ch, move, fade, scale);
            oneLetter.setDelay(Duration.millis(i * 90));

            construct.getChildren().add(oneLetter);
        }

        ScaleTransition pulseUp = new ScaleTransition(Duration.seconds(0.35), titleNode);
        pulseUp.setToX(1.06);
        pulseUp.setToY(1.06);

        ScaleTransition pulseDown = new ScaleTransition(Duration.seconds(0.35), titleNode);
        pulseDown.setToX(1.0);
        pulseDown.setToY(1.0);

        FadeTransition subFade = new FadeTransition(Duration.seconds(0.75), subtitle);
        subFade.setFromValue(0);
        subFade.setToValue(1);

        TranslateTransition subMove = new TranslateTransition(Duration.seconds(0.75), subtitle);
        subMove.setToY(0);

        PauseTransition pause = new PauseTransition(Duration.seconds(1.1));

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.9), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition full = new SequentialTransition(
                construct,
                pulseUp,
                new ParallelTransition(pulseDown, subFade, subMove),
                pause,
                fadeOut
        );

        full.setOnFinished(e -> onFinished.run());
        full.play();
    }
}