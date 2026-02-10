package com.equationplotter.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GraphCanvas extends Canvas {

    private double xCenter = 0;
    private double yCenter = 0;

    // 1 unit = কত px (zoom later)
    private double unitPx = 50;

    private final int minorStep = 1;
    private final int majorStep = 5;

    public GraphCanvas() {
        // resize হলে redraw
        widthProperty().addListener((o,a,b)->draw());
        heightProperty().addListener((o,a,b)->draw());
    }

    @Override
    public boolean isResizable() { return true; }

    @Override
    public void resize(double w, double h) {
        setWidth(w);
        setHeight(h);
        draw();
    }

    private double wxToPx(double x) {
        double cx = getWidth() / 2.0;
        return cx + (x - xCenter) * unitPx;
    }

    private double wyToPy(double y) {
        double cy = getHeight() / 2.0;
        return cy - (y - yCenter) * unitPx;
    }

    public void draw() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext g = getGraphicsContext2D();

        // background
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        // visible world range (depends on window size + unitPx)
        double halfWUnits = (w / 2.0) / unitPx;
        double halfHUnits = (h / 2.0) / unitPx;

        int xMin = (int) Math.floor(xCenter - halfWUnits);
        int xMax = (int) Math.ceil (xCenter + halfWUnits);
        int yMin = (int) Math.floor(yCenter - halfHUnits);
        int yMax = (int) Math.ceil (yCenter + halfHUnits);

        // -------- Minor grid (1x1) --------
        g.setStroke(Color.web("#eeeeee"));
        g.setLineWidth(1);

        for (int x = xMin; x <= xMax; x += minorStep) {
            double px = wxToPx(x);
            g.strokeLine(px, 0, px, h);
        }
        for (int y = yMin; y <= yMax; y += minorStep) {
            double py = wyToPy(y);
            g.strokeLine(0, py, w, py);
        }

        // -------- Major grid (5x5) – MUST start from (0,0) --------
        g.setStroke(Color.web("#c9c9c9"));
        g.setLineWidth(1.4);

        int firstMajorX = (int) (Math.ceil(xMin / (double)majorStep) * majorStep);
        int firstMajorY = (int) (Math.ceil(yMin / (double)majorStep) * majorStep);

        for (int x = firstMajorX; x <= xMax; x += majorStep) {
            double px = wxToPx(x);
            g.strokeLine(px, 0, px, h);
        }
        for (int y = firstMajorY; y <= yMax; y += majorStep) {
            double py = wyToPy(y);
            g.strokeLine(0, py, w, py);
        }

        // -------- Axes (superimposed exactly on grid lines) --------
        g.setStroke(Color.web("#666666"));
        g.setLineWidth(2.6);

        double xAxisY = wyToPy(0);
        double yAxisX = wxToPx(0);

        g.strokeLine(0, xAxisY, w, xAxisY);   // X-axis
        g.strokeLine(yAxisX, 0, yAxisX, h);   // Y-axis

        // -------- Numbering: like Desmos (major ticks only) --------
        g.setFill(Color.web("#444444"));
        g.setFont(Font.font(12));

        // x labels on x-axis (every 2 or 5; you want -10..10 style, so do 2)
        for (int x = -10; x <= 10; x += 2) {
            double px = wxToPx(x);
            g.fillText(String.valueOf(x), px + 2, xAxisY + 14);
        }

        // y labels on y-axis (-6..6)
        for (int y = -6; y <= 6; y += 2) {
            if (y == 0) continue;
            double py = wyToPy(y);
            g.fillText(String.valueOf(y), yAxisX + 6, py - 2);
        }
    }
}

//package com.equationplotter.ui;
//import javafx.scene.text.Font;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.paint.Color;
//
//public class GraphCanvas extends Canvas {
//    private double xMin=-10,xMax=10;
//    private double yMin=-6,yMax=6;
//    // grid
//    private int minorStep=1;
//    private int majorStep=5;
//    @Override
//    public boolean isResizable() {
//        return true;
//    }
//
//    @Override
//    public double prefWidth(double height) {
//        return getWidth();
//    }
//
//    @Override
//    public double prefHeight(double width) {
//        return getHeight();
//    }
//
//    @Override
//    public void resize(double width, double height) {
//        setWidth(width);
//        setHeight(height);
//        draw();
//    }
//
//
//    // grid settings
//    private double gridStep = 40;   // grid line distance (px)
//    private double axisWidth = 2.5; // axes thickness
//    private double gridWidth = 1.0; // normal grid thickness
//
//    public GraphCanvas(double width, double height) {
//        super(width, height);
//
//        // redraw when resized
//        widthProperty().addListener((obs, oldV, newV) -> draw());
//        heightProperty().addListener((obs, oldV, newV) -> draw());
//
//        draw();
//    }
//    public void draw() {
//        double w = getWidth();
//        double h = getHeight();
//
//        GraphicsContext g = getGraphicsContext2D();
//
//        // background
//        g.setFill(Color.WHITE);
//        g.fillRect(0, 0, w, h);
//
//        // pixels per unit
//        double sx = w / (xMax - xMin);
//        double sy = h / (yMax - yMin);
//
//        // convert world -> screen
//        // screenX = (x - xMin) * sx
//        // screenY = (yMax - y) * sy   (because y goes up, screen goes down)
//
//        // -------- Minor Grid (1 unit) light --------
//        g.setStroke(Color.web("#eeeeee"));
//        g.setLineWidth(1);
//
//        for (int x = (int) xMin; x <= (int) xMax; x += minorStep) {
//            double px = (x - xMin) * sx;
//            g.strokeLine(px, 0, px, h);
//        }
//        for (int y = (int) yMin; y <= (int) yMax; y += minorStep) {
//            double py = (yMax - y) * sy;
//            g.strokeLine(0, py, w, py);
//        }
//
//        // -------- Major Grid (5 unit) darker --------
//        g.setStroke(Color.web("#cccccc"));
//        g.setLineWidth(1.3);
//
//        for (int x = (int) xMin; x <= (int) xMax; x += majorStep) {
//            double px = (x - xMin) * sx;
//            g.strokeLine(px, 0, px, h);
//        }
//        for (int y = (int) yMin; y <= (int) yMax; y += majorStep) {
//            double py = (yMax - y) * sy;
//            g.strokeLine(0, py, w, py);
//        }
//
//        // -------- Axes (bold) --------
//        double xAxisY = (yMax - 0) * sy;      // y=0
//        double yAxisX = (0 - xMin) * sx;      // x=0
//
//        g.setStroke(Color.web("#666666"));
//        g.setLineWidth(2.6);
//        g.strokeLine(0, xAxisY, w, xAxisY);   // X axis
//        g.strokeLine(yAxisX, 0, yAxisX, h);   // Y axis
//
//        // -------- Numbering on axes --------
//        g.setFill(Color.web("#444444"));
//        g.setFont(javafx.scene.text.Font.font(12));
//
//        // x-axis labels: -10 to +10
//        for (int x = (int) xMin; x <= (int) xMax; x += 2) { // 2 step দিলে clean দেখায়
//            double px = (x - xMin) * sx;
//            // label near x-axis line
//            g.fillText(String.valueOf(x), px + 2, xAxisY + 14);
//        }
//
//        // y-axis labels: -6 to +6
//        for (int y = (int) yMin; y <= (int) yMax; y += 2) {
//            double py = (yMax - y) * sy;
//            // label near y-axis line
//            if (y != 0) { // 0 বাদ দিলে clutter কমে
//                g.fillText(String.valueOf(y), yAxisX + 6, py - 2);
//            }
//        }
//    }
//
//
//}
