package com.equationplotter.ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.Random;

public class AboutPage extends StackPane {

    private static final Random random = new Random();

    public AboutPage(Runnable onBack) {

        this.setStyle("-fx-background-color: #0B0715;"); // Dark background matching the menu theme

        // --- BACKGROUND EFFECT LAYER (Floating Symbols) ---
        Pane backgroundEffectLayer = new Pane();
        backgroundEffectLayer.setMouseTransparent(true);
        this.getChildren().add(backgroundEffectLayer);

        // Populate with floating mathematical symbols
        String[] symbols = {
                "∫", "∮", "∂", "dy/dx", "lim", "∑", "∏", "√",
                "∞", "π", "θ", "α", "β", "λ", "μ", "Δ", "∇",
                "≈", "≠", "±", "×", "÷", "∈", "∪", "∩",
                "sin", "cos", "tan", "log", "ln", "e^x", "f(x)", "x²"
        };
        for (int i = 0; i < 20; i++) {
            Label symbol = createFloatingSymbol(symbols[random.nextInt(symbols.length)]);
            backgroundEffectLayer.getChildren().add(symbol);
            animateFloatingSymbol(symbol);
        }

        // Populate with some glowing, floating mathematical orbs
        for (int i = 0; i < 15; i++) {
            javafx.scene.shape.Circle glow = new javafx.scene.shape.Circle(25 + random.nextInt(40));
            glow.setManaged(false);
            Color[] glows = { Color.web("#FF28B4", 0.06), Color.web("#28C8FF", 0.06), Color.web("#A028FF", 0.06) };
            glow.setFill(glows[random.nextInt(glows.length)]);
            glow.setEffect(new GaussianBlur(40));
            glow.setLayoutX(50 + random.nextInt(1050));
            glow.setLayoutY(50 + random.nextInt(850));
            TranslateTransition move = new TranslateTransition(Duration.seconds(15 + random.nextInt(10)), glow);
            move.setByX(-80 + random.nextInt(160));
            move.setByY(-80 + random.nextInt(160));
            move.setAutoReverse(true);
            move.setCycleCount(Animation.INDEFINITE);
            move.setInterpolator(Interpolator.EASE_BOTH);
            new ParallelTransition(move).play();
            backgroundEffectLayer.getChildren().add(glow);
        }

        // --- MAIN CONTENT CONTAINER ---
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40, 20, 60, 20));
        mainContainer.setStyle("-fx-background-color: transparent;");
        mainContainer.setMaxWidth(850);

        // This wrapper forces the mainContainer to stay horizontally centered inside the ScrollPane
        StackPane centerWrapper = new StackPane(mainContainer);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: transparent;");

        // --- BACK BUTTON ---
        Button backBtn = new Button("← Back to Menu");
        backBtn.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.08); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: #FF61D2; -fx-border-radius: 20; -fx-border-width: 1.5; " +
                        "-fx-cursor: hand;"
        );
        backBtn.setPadding(new Insets(8, 20, 8, 20));
        backBtn.setOnAction(e -> onBack.run());

        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: rgba(255, 97, 210, 0.25); -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-border-color: #FF61D2; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-cursor: hand;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-border-color: #FF61D2; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-cursor: hand;"));

        HBox backBox = new HBox(backBtn);
        backBox.setAlignment(Pos.CENTER_LEFT); // Align back button to the left edge of the centered block
        backBox.setPadding(new Insets(0, 0, 10, 0));

        // --- TITLE ---
        Text titleText = new Text("About Equation Plotter");
        titleText.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 52));
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FF61D2")),
                new Stop(1, Color.web("#9B4DFF"))
        );
        titleText.setFill(gradient);
        titleText.setEffect(new DropShadow(55, Color.rgb(155, 77, 255, 0.45)));

        VBox titleBox = new VBox(titleText);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 30, 0));

        // --- SECTIONS ---
        VBox overviewCard = createCard("Overview",
                "The Equation Plotter is a dynamic and interactive visualization tool designed to help users explore mathematical equations in multiple dimensions. It provides intuitive graphical representations, making complex mathematical concepts easier to understand and analyze.");

        VBox featuresCard = createFeaturesCard();

        VBox philosophyCard = createCard("Design Philosophy",
                "This application combines functionality with elegance, ensuring a smooth and visually appealing user experience. The interface is designed to be intuitive, modern, and responsive, helping users focus on learning and exploration.");

        VBox creditsCard = createCreditsCard();

        // Assemble everything into the main container
        mainContainer.getChildren().addAll(backBox, titleBox, overviewCard, featuresCard, philosophyCard, creditsCard);

        // Wrap the centered wrapper in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(centerWrapper);
        scrollPane.setFitToWidth(true); // Ensures the wrapper takes full width to center its contents
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.getStylesheets().add("data:text/css,.scroll-pane > .viewport { -fx-background-color: transparent; }");

        this.getChildren().add(scrollPane);
    }

    // --- HELPER METHODS FOR UI COMPONENTS ---

    private static VBox createCard(String headerTitle, String body) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_LEFT); // Keep block contents clean and left aligned
        card.setPadding(new Insets(30));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06); " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0, 0, 8);"
        );

        card.setBorder(new Border(new BorderStroke(
                new LinearGradient(
                        0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.00, Color.web("#FF61D2", 0.5)),
                        new Stop(1.00, Color.web("#42C8FF", 0.5))
                ),
                BorderStrokeStyle.SOLID,
                new CornerRadii(20),
                new BorderWidths(2)
        )));

        Label header = new Label(headerTitle);
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        header.setTextFill(Color.rgb(255, 255, 255, 0.95));

        Label content = new Label(body);
        content.setWrapText(true);
        content.setFont(Font.font("Segoe UI", 18));
        content.setTextAlignment(TextAlignment.LEFT); // Text remains left-aligned
        content.setTextFill(Color.rgb(220, 225, 240, 0.9));
        content.setStyle("-fx-line-spacing: 6px;");

        card.getChildren().addAll(header, content);
        return card;
    }

    private static VBox createFeaturesCard() {
        VBox card = new VBox(25);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(30));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06); " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0, 0, 8);"
        );

        card.setBorder(new Border(new BorderStroke(
                new LinearGradient(
                        0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.00, Color.web("#FF61D2", 0.5)),
                        new Stop(1.00, Color.web("#42C8FF", 0.5))
                ),
                BorderStrokeStyle.SOLID,
                new CornerRadii(20),
                new BorderWidths(2)
        )));

        Label header = new Label("Features");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        header.setTextFill(Color.rgb(255, 255, 255, 0.95));

        card.getChildren().add(header);

        card.getChildren().add(createFeatureSection("🔹 2D Graph Plotting",
                "• Plot standard mathematical functions (e.g., linear, quadratic, trigonometric).\n" +
                        "• Real-time graph updates based on user input.\n" +
                        "• Zoom and pan functionality for detailed inspection.\n" +
                        "• Coordinate tracking for precise value analysis."));

        card.getChildren().add(createFeatureSection("🔹 3D Surface Visualization",
                "• Render three-dimensional surfaces for equations involving two variables.\n" +
                        "• Rotate and explore graphs interactively from different angles.\n" +
                        "• Enhanced depth perception for better understanding of spatial relationships.\n" +
                        "• Smooth rendering for visually appealing output."));

        card.getChildren().add(createFeatureSection("🔹 Polar Graphing",
                "• Plot equations in polar coordinates (r = f(θ)).\n" +
                        "• Ideal for visualizing circular, spiral, and periodic patterns.\n" +
                        "• Dynamic angle-based rendering.\n" +
                        "• Supports a wide range of polar functions."));

        return card;
    }

    private static VBox createFeatureSection(String title, String points) {
        VBox section = new VBox(8);
        section.setAlignment(Pos.TOP_LEFT);

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTitle.setTextFill(Color.web("#9B4DFF"));

        Label lblPoints = new Label(points);
        lblPoints.setWrapText(true);
        lblPoints.setFont(Font.font("Segoe UI", 18));
        lblPoints.setTextAlignment(TextAlignment.LEFT);
        lblPoints.setTextFill(Color.rgb(220, 225, 240, 0.9));
        lblPoints.setStyle("-fx-line-spacing: 5px;");

        section.getChildren().addAll(lblTitle, lblPoints);
        return section;
    }

    private static VBox createCreditsCard() {
        VBox card = new VBox(35);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(30));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06); " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(155, 77, 255, 0.25), 30, 0, 0, 8);"
        );

        card.setBorder(new Border(new BorderStroke(
                new LinearGradient(
                        0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.00, Color.web("#FF61D2", 0.7)),
                        new Stop(1.00, Color.web("#42C8FF", 0.7))
                ),
                BorderStrokeStyle.SOLID,
                new CornerRadii(20),
                new BorderWidths(2.5)
        )));

        Label header = new Label("Credits");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        header.setTextFill(Color.rgb(255, 255, 255, 0.95));
        card.getChildren().add(header);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER_LEFT);
        grid.setHgap(45);
        grid.setVgap(18);

        addCreditRow(grid, 0, "Developed By", "MD. ATIK KHAN\nDEBABRATA DAS RAHUL");
        addCreditRow(grid, 1, "Supervisor", "MD NURUL MUTTAKIN");
        addCreditRow(grid, 2, "Institution", "Bangladesh University of Engineering & Technology (BUET)");

        VBox ackBox = new VBox(10);
        ackBox.setAlignment(Pos.TOP_LEFT);

        Label ackTitle = new Label("Acknowledgment");
        ackTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        ackTitle.setTextFill(Color.rgb(255, 255, 255, 0.9));

        Label ackText = new Label("We express our sincere gratitude to our supervisor for guidance and support throughout the development of this project.");
        ackText.setWrapText(true);
        ackText.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 16));
        ackText.setTextAlignment(TextAlignment.LEFT);
        ackText.setTextFill(Color.rgb(180, 190, 220, 0.85));
        ackBox.getChildren().addAll(ackTitle, ackText);

        card.getChildren().addAll(grid, ackBox);
        return card;
    }

    private static void addCreditRow(GridPane grid, int row, String role, String names) {
        Label lblRole = new Label(role);
        lblRole.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblRole.setTextFill(Color.web("#FF61D2"));

        Label lblNames = new Label(names);
        lblNames.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblNames.setTextAlignment(TextAlignment.LEFT);
        lblNames.setTextFill(Color.rgb(255, 255, 255, 0.95));

        grid.add(lblRole, 0, row);
        grid.add(lblNames, 1, row);
    }

    private static Label createFloatingSymbol(String text) {
        Label label = new Label(text);
        int fontSize = 24 + random.nextInt(20);
        label.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        Color[] neonColors = { Color.web("#FF61D2"), Color.web("#9B4DFF"), Color.web("#42C8FF"), Color.web("#F0F0FF") };
        Color selectedColor = neonColors[random.nextInt(neonColors.length)];
        label.setTextFill(selectedColor);
        label.setEffect(new GaussianBlur(3.5));
        label.setRotate(random.nextInt(360));
        label.setLayoutX(20 + random.nextInt(1050));
        label.setLayoutY(20 + random.nextInt(800));
        return label;
    }

    private static void animateFloatingSymbol(Label label) {
        int range = 45;
        TranslateTransition drift = new TranslateTransition(Duration.seconds(16 + random.nextDouble() * 8), label);
        drift.setByX(-range + random.nextInt(range * 2));
        drift.setByY(-range + random.nextInt(range * 2));
        drift.setCycleCount(Animation.INDEFINITE);
        drift.setAutoReverse(true);
        drift.setInterpolator(Interpolator.EASE_BOTH);
        FadeTransition fade = new FadeTransition(Duration.seconds(10 + random.nextDouble() * 6), label);
        fade.setFromValue(0.1);
        fade.setToValue(0.3);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);
        new ParallelTransition(drift, fade).play();
    }
}