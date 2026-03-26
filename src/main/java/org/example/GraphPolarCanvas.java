package com.equationplotter.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class GraphPolarCanvas extends Canvas {

    private final List<PlotPolarEquation> equations = new ArrayList<>();
    private boolean isDarkMode = false;

    private double xCenter = 0;
    private double yCenter = 0;
    private double unitPx = 50;
    private double lastMouseX, lastMouseY;

    public GraphPolarCanvas() {
        setWidth(800); setHeight(600);
        widthProperty().addListener((o, a, b) -> draw());
        heightProperty().addListener((o, a, b) -> draw());

        this.setOnScroll((ScrollEvent event) -> {
            if (event.getDeltaY() == 0) return;
            double scale = (event.getDeltaY() > 0) ? 1.05 : (1 / 1.05);
            double mouseWx = pxToWx(event.getX());
            double mouseWy = pxToWy(event.getY());
            unitPx *= scale;
            if (unitPx < 2) unitPx = 2;
            if (unitPx > 10000) unitPx = 10000;
            xCenter = mouseWx - (event.getX() - getWidth() / 2.0) / unitPx;
            yCenter = mouseWy + (event.getY() - getHeight() / 2.0) / unitPx;
            draw();
            event.consume();
        });

        this.setOnMousePressed(e -> { lastMouseX = e.getX(); lastMouseY = e.getY(); });
        this.setOnMouseDragged(e -> {
            xCenter -= (e.getX() - lastMouseX) / unitPx;
            yCenter += (e.getY() - lastMouseY) / unitPx;
            lastMouseX = e.getX(); lastMouseY = e.getY();
            draw();
        });
        draw();
    }

    public void setEquations(List<PlotPolarEquation> eqs) {
        equations.clear();
        if (eqs != null) equations.addAll(eqs);
        draw();
    }

    public void setDarkMode(boolean isDark) { this.isDarkMode = isDark; draw(); }
    public void resetView() { this.xCenter = 0; this.yCenter = 0; this.unitPx = 50; draw(); }

    @Override public boolean isResizable() { return true; }
    @Override public void resize(double w, double h) { setWidth(w); setHeight(h); draw(); }

    private double wxToPx(double x) { return (getWidth() / 2.0) + (x - xCenter) * unitPx; }
    private double wyToPy(double y) { return (getHeight() / 2.0) - (y - yCenter) * unitPx; }
    private double pxToWx(double px) { return (px - getWidth() / 2.0) / unitPx + xCenter; }
    private double pxToWy(double py) { return (getHeight() / 2.0 - py) / unitPx + yCenter; }

    public void draw() {
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        GraphicsContext g = getGraphicsContext2D();

        Color bgColor = isDarkMode ? Color.web("#121212") : Color.WHITE;
        Color gridColor = isDarkMode ? Color.web("#333333") : Color.web("#eeeeee");
        Color axisColor = isDarkMode ? Color.web("#aaaaaa") : Color.web("#666666");
        Color textColor = isDarkMode ? Color.web("#aaaaaa") : Color.web("#888888");

        g.setFill(bgColor); g.fillRect(0, 0, w, h);
        g.setLineCap(StrokeLineCap.ROUND); g.setLineJoin(StrokeLineJoin.ROUND);

        // 1. Draw Polar Grid (Concentric Circles & Radial Lines)
        double maxRadiusPixels = Math.sqrt(w*w + h*h) / 2.0 + Math.abs(xCenter*unitPx) + Math.abs(yCenter*unitPx);
        int maxR = (int) Math.ceil(maxRadiusPixels / unitPx);

        g.setStroke(gridColor);
        g.setLineWidth(1);
        double originX = wxToPx(0);
        double originY = wyToPy(0);

        // Circles
        for (int r = 1; r <= maxR; r++) {
            double radiusPx = r * unitPx;
            g.strokeOval(originX - radiusPx, originY - radiusPx, radiusPx * 2, radiusPx * 2);
        }

        // Radial Lines (every 30 degrees / pi/6)
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            double endX = originX + maxRadiusPixels * Math.cos(angle);
            double endY = originY - maxRadiusPixels * Math.sin(angle);
            g.strokeLine(originX, originY, endX, endY);
        }

        // Axes
        g.setStroke(axisColor); g.setLineWidth(1.5);
        g.strokeLine(0, originY, w, originY);
        g.strokeLine(originX, 0, originX, h);

        // Grid Numbers
        g.setFill(textColor); g.setFont(Font.font("Arial", 11));
        for (int r = 1; r <= maxR; r+=2) { // Label every other circle
            g.fillText(String.valueOf(r), originX + (r * unitPx) + 2, originY - 4);
        }

        // 2. Plot Polar Equations
        g.setLineWidth(2.2);
        for (PlotPolarEquation eq : equations) {
            if (eq == null || !eq.isVisible()) continue;
            g.setStroke(eq.getColor() == null ? Color.BLUE : eq.getColor());

            // Plot from 0 to 24 PI (allows complex overlapping shapes to complete)
            double tMin = 0;
            double tMax = 24 * Math.PI;
            double step = 0.02; // Very high resolution for smooth curves

            boolean penDown = false;
            for (double t = tMin; t <= tMax; t += step) {
                double r = eq.eval(t);
                if (Double.isNaN(r) || Double.isInfinite(r)) {
                    if (penDown) { g.stroke(); penDown = false; }
                    continue;
                }

                // Convert Polar (r, t) to Cartesian (x, y)
                double x = r * Math.cos(t);
                double y = r * Math.sin(t);

                if (!penDown) {
                    g.beginPath();
                    g.moveTo(wxToPx(x), wyToPy(y));
                    penDown = true;
                } else {
                    g.lineTo(wxToPx(x), wyToPy(y));
                }
            }
            if (penDown) g.stroke();
        }
    }
}