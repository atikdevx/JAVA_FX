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
    private int niceStep(double units) {
        // units = about how many numbers visible on axis
        // returns a "nice" step: 1,2,5,10,20...
        if (units <= 10) return 1;
        if (units <= 20) return 2;
        if (units <= 50) return 5;
        if (units <= 100) return 10;
        if (units <= 200) return 20;
        return 50;
    }

    private int floorToStep(double v, int step) {
        return (int) Math.floor(v / step) * step;
    }

    private int ceilToStep(double v, int step) {
        return (int) Math.ceil(v / step) * step;
    }
    public GraphCanvas() {
        // initial safe size (0 হলে draw skip হয়, তাই পরে parent থেকে set হবে)
        setWidth(800);
        setHeight(600);

        // redraw when width/height changes
        widthProperty().addListener((o, a, b) -> draw());
        heightProperty().addListener((o, a, b) -> draw());

        draw();
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
        // -------- Dynamic Numbering (fills whole screen) --------
        g.setFill(Color.web("#444444"));
        g.setFont(Font.font(12));

// visible world range based on window size
        double xVisMin = xCenter - halfWUnits;
        double xVisMax = xCenter + halfWUnits;

        double yVisMin = yCenter - halfHUnits;
        double yVisMax = yCenter + halfHUnits;

// choose label step so it doesn't become too crowded
        int xStep = niceStep(xVisMax - xVisMin);
        int yStep = niceStep(yVisMax - yVisMin);

// start/end aligned to step
        int xStart = floorToStep(xVisMin, xStep);
        int xEnd   = ceilToStep(xVisMax, xStep);

        int yStart = floorToStep(yVisMin, yStep);
        int yEnd   = ceilToStep(yVisMax, yStep);

// X-axis labels (left to right)
        for (int x = xStart; x <= xEnd; x += xStep) {
            double px = wxToPx(x);
            // show label only if within screen
            if (px >= 0 && px <= w) {
                g.fillText(String.valueOf(x), px + 2, xAxisY + 14);
            }
        }

// Y-axis labels (top to bottom)
        for (int y = yStart; y <= yEnd; y += yStep) {
            if (y == 0) continue; // 0 বাদ দিলে clean দেখায়
            double py = wyToPy(y);
            if (py >= 0 && py <= h) {
                g.fillText(String.valueOf(y), yAxisX + 6, py - 2);
            }
        }
        // x labels on x-axis (every 2 or 5; you want -10..10 style, so do 2)
    }
}