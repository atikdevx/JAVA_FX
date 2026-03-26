package com.equationplotter.ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Random;

public class HomeView extends StackPane {

    private final Random random = new Random();

    // CHANGED: Added "Runnable onStartPolarPlotting" to the constructor
    public HomeView(Runnable onStartPlotting, Runnable onStart3DPlotting, Runnable onStartPolarPlotting) {

        setBackground(new Background(new BackgroundFill(
                new LinearGradient(
                        0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.00, Color.web("#05050A")),
                        new Stop(0.35, Color.web("#0F0C29")),
                        new Stop(0.65, Color.web("#1F103A")),
                        new Stop(1.00, Color.web("#0B0715"))
                ),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        Region overlay1 = new Region();
        overlay1.setBackground(new Background(new BackgroundFill(
                new RadialGradient(
                        0, 0, 0.15, 0.15, 0.65, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(180, 20, 150, 0.25)),
                        new Stop(1, Color.rgb(180, 20, 150, 0.0))
                ),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        Region overlay2 = new Region();
        overlay2.setBackground(new Background(new BackgroundFill(
                new RadialGradient(
                        0, 0, 0.85, 0.85, 0.65, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(10, 150, 255, 0.20)),
                        new Stop(1, Color.rgb(10, 150, 255, 0.0))
                ),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        Pane backSymbolLayer = new Pane();
        backSymbolLayer.setMouseTransparent(true);

        Pane frontSymbolLayer = new Pane();
        frontSymbolLayer.setMouseTransparent(true);

        String[] symbols = {
                "∫", "∮", "∂", "dy/dx", "lim", "∑", "∏", "√",
                "∞", "π", "θ", "α", "β", "λ", "μ", "Δ", "∇",
                "≈", "≠", "±", "×", "÷", "∈", "∪", "∩",
                "sin", "cos", "tan", "log", "ln", "e^x", "f(x)", "x²"
        };

        for (int i = 0; i < 35; i++) {
            Label symbol = createFloatingSymbol(symbols[random.nextInt(symbols.length)], true);
            backSymbolLayer.getChildren().add(symbol);
            animateFloatingSymbol(symbol, true);
        }

        for (int i = 0; i < 40; i++) {
            Label symbol = createFloatingSymbol(symbols[random.nextInt(symbols.length)], false);
            frontSymbolLayer.getChildren().add(symbol);
            animateFloatingSymbol(symbol, false);
        }

        for (int i = 0; i < 25; i++) {
            Circle glow = new Circle(15 + random.nextInt(45));
            glow.setManaged(false);

            Color[] glows = {
                    Color.web("#FF28B4", 0.1),
                    Color.web("#28C8FF", 0.1),
                    Color.web("#A028FF", 0.1)
            };

            glow.setFill(glows[random.nextInt(glows.length)]);
            glow.setEffect(new GaussianBlur(35));
            glow.setLayoutX(50 + random.nextInt(1050));
            glow.setLayoutY(50 + random.nextInt(850));

            TranslateTransition move = new TranslateTransition(Duration.seconds(12 + random.nextInt(10)), glow);
            move.setByX(-60 + random.nextInt(120));
            move.setByY(-60 + random.nextInt(120));
            move.setAutoReverse(true);
            move.setCycleCount(Animation.INDEFINITE);
            move.setInterpolator(Interpolator.EASE_BOTH);

            FadeTransition fade = new FadeTransition(Duration.seconds(7 + random.nextInt(6)), glow);
            fade.setFromValue(0.20);
            fade.setToValue(0.80);
            fade.setAutoReverse(true);
            fade.setCycleCount(Animation.INDEFINITE);

            new ParallelTransition(move, fade).play();
            backSymbolLayer.getChildren().add(glow);
        }

        VBox leftContent = new VBox(22);
        leftContent.setAlignment(Pos.CENTER_LEFT);
        leftContent.setPadding(new Insets(65));

        Label title = new Label("Pika Plotter");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 68));
        title.setTextFill(new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#FF61D2")),
                new Stop(0.40, Color.web("#9B4DFF")),
                new Stop(1.00, Color.web("#42C8FF"))
        ));
        title.setEffect(new DropShadow(45, Color.rgb(155, 77, 255, 0.45)));

        Label subtitle = new Label("A beautiful space for infinite mathematical visions.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 21));
        subtitle.setTextFill(Color.rgb(220, 225, 240, 0.88));

        VBox features = new VBox(14,
                createFeature("✦ Dynamic mathematical environment"),
                createFeature("✦ Immersive 3D depth effect"),
                createFeature("✦ Premium glassmorphism interface"),
                createFeature("✦ Fluid & elegant visual experience")
        );

        leftContent.getChildren().addAll(title, subtitle, features);

        VBox menuPanel = new VBox(22);
        menuPanel.setAlignment(Pos.CENTER);
        menuPanel.setPadding(new Insets(45));
        menuPanel.setPrefWidth(350);
        menuPanel.setMaxWidth(350);

        menuPanel.setBackground(new Background(new BackgroundFill(
                new LinearGradient(
                        0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.00, Color.rgb(45, 25, 80, 0.28)),
                        new Stop(1.00, Color.rgb(20, 15, 45, 0.45))
                ),
                new CornerRadii(35),
                Insets.EMPTY
        )));

        menuPanel.setBorder(new Border(new BorderStroke(
                new LinearGradient(
                        0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.00, Color.rgb(255, 97, 210, 0.35)),
                        new Stop(1.00, Color.rgb(66, 200, 255, 0.35))
                ),
                BorderStrokeStyle.SOLID,
                new CornerRadii(35),
                new BorderWidths(1.5)
        )));

        menuPanel.setEffect(new DropShadow(55, Color.rgb(0, 0, 0, 0.65)));

        Label menuTitle = new Label("MENU");
        menuTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        menuTitle.setTextFill(Color.rgb(255, 255, 255, 0.95));
        menuTitle.setEffect(new DropShadow(20, Color.rgb(255, 255, 255, 0.25)));

        Button startBtn = createMenuButton("2D Graph", true);
        Button start3DBtn = createMenuButton("3D Graph", false);

        // CHANGED: Added the Polar Graph Button
        Button startPolarBtn = createMenuButton("Polar Graph", false);

        Button aboutBtn = createMenuButton("About", false);
        Button exitBtn = createMenuButton("Exit", false);

        startBtn.setOnAction(e -> onStartPlotting.run());
        start3DBtn.setOnAction(e -> onStart3DPlotting.run());

        // CHANGED: Hooked up the Polar Button action
        startPolarBtn.setOnAction(e -> onStartPolarPlotting.run());

        aboutBtn.setOnAction(e -> System.out.println("About clicked"));
        exitBtn.setOnAction(e -> javafx.application.Platform.exit());

        // CHANGED: Added startPolarBtn to the VBox
        menuPanel.getChildren().addAll(menuTitle, startBtn, start3DBtn, startPolarBtn, aboutBtn, exitBtn);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(30, 70, 30, 40));
        mainLayout.setLeft(leftContent);
        mainLayout.setRight(menuPanel);

        BorderPane.setAlignment(leftContent, Pos.CENTER_LEFT);
        BorderPane.setAlignment(menuPanel, Pos.CENTER_RIGHT);

        getChildren().addAll(
                overlay1, overlay2,
                backSymbolLayer, frontSymbolLayer,
                mainLayout
        );

        overlay1.prefWidthProperty().bind(widthProperty());
        overlay1.prefHeightProperty().bind(heightProperty());
        overlay2.prefWidthProperty().bind(widthProperty());
        overlay2.prefHeightProperty().bind(heightProperty());
    }

    private Label createFloatingSymbol(String text, boolean backLayer) {
        Label label = new Label(text);

        int fontSize = backLayer ? 20 + random.nextInt(16) : 28 + random.nextInt(22);
        label.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));

        Color[] neonColors = {
                Color.web("#FF61D2"),
                Color.web("#9B4DFF"),
                Color.web("#42C8FF"),
                Color.web("#F0F0FF")
        };

        Color selectedColor = neonColors[random.nextInt(neonColors.length)];
        label.setTextFill(selectedColor);

        if (!backLayer) {
            DropShadow neonGlow = new DropShadow(25, selectedColor);
            neonGlow.setSpread(0.4);
            label.setEffect(neonGlow);
        } else {
            label.setEffect(new GaussianBlur(3.0));
        }

        label.setRotate(random.nextInt(360));
        label.setLayoutX(20 + random.nextInt(1050));
        label.setLayoutY(20 + random.nextInt(800));

        return label;
    }

    private void animateFloatingSymbol(Label label, boolean backLayer) {
        int range = backLayer ? 35 : 70;
        double durationBase = backLayer ? 15 : 10;

        TranslateTransition drift = new TranslateTransition(Duration.seconds(durationBase + random.nextDouble() * 6), label);
        drift.setByX(-range + random.nextInt(range * 2));
        drift.setByY(-range + random.nextInt(range * 2));
        drift.setCycleCount(Animation.INDEFINITE);
        drift.setAutoReverse(true);
        drift.setInterpolator(Interpolator.EASE_BOTH);

        RotateTransition rotate = new RotateTransition(Duration.seconds(12 + random.nextDouble() * 10), label);
        rotate.setByAngle(random.nextBoolean() ? 45 : -45);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setAutoReverse(true);
        rotate.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fade = new FadeTransition(Duration.seconds(8 + random.nextDouble() * 5), label);
        fade.setFromValue(backLayer ? 0.2 : 0.45);
        fade.setToValue(backLayer ? 0.55 : 0.95);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);

        ScaleTransition breathe = new ScaleTransition(Duration.seconds(7 + random.nextDouble() * 4), label);
        breathe.setToX(1.15);
        breathe.setToY(1.15);
        breathe.setCycleCount(Animation.INDEFINITE);
        breathe.setAutoReverse(true);
        breathe.setInterpolator(Interpolator.EASE_BOTH);

        new ParallelTransition(drift, rotate, fade, breathe).play();
    }

    private Label createFeature(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        label.setTextFill(Color.rgb(180, 190, 220, 0.95));
        return label;
    }

    private Button createMenuButton(String text, boolean isPrimary) {
        Button btn = new Button(text);
        btn.setPrefWidth(270);
        btn.setPrefHeight(55);

        String baseColor = isPrimary ?
                "linear-gradient(to bottom right, rgba(138, 43, 226, 0.65), rgba(75, 0, 130, 0.85))" :
                "linear-gradient(to bottom right, rgba(40, 45, 80, 0.55), rgba(20, 25, 45, 0.75))";

        String hoverColor = isPrimary ?
                "linear-gradient(to bottom right, rgba(158, 63, 246, 0.85), rgba(95, 20, 150, 0.95))" :
                "linear-gradient(to bottom right, rgba(60, 65, 110, 0.75), rgba(30, 35, 65, 0.85))";

        String borderColor = isPrimary ?
                "linear-gradient(to right, rgba(255, 97, 210, 0.6), rgba(66, 200, 255, 0.6))" :
                "rgba(255, 255, 255, 0.2)";

        String baseStyle =
                "-fx-background-color: " + baseColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 28;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-radius: 28;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-cursor: hand;";

        String hoverStyle =
                "-fx-background-color: " + hoverColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 28;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.5);" +
                        "-fx-border-radius: 28;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-cursor: hand;";

        btn.setStyle(baseStyle);
        btn.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.45)));

        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
            btn.setEffect(new DropShadow(30,
                    isPrimary ? Color.rgb(155, 77, 255, 0.6) : Color.rgb(100, 150, 255, 0.4)));
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
            btn.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.45)));
        });

        btn.setOnMousePressed(e -> {
            btn.setScaleX(0.96);
            btn.setScaleY(0.96);
        });

        btn.setOnMouseReleased(e -> {
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
        });

        return btn;
    }
}