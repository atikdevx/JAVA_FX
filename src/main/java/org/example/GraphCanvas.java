package com.equationplotter.ui;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class GraphCanvas extends Canvas {

    // ---------- Equations ----------
    private final List<PlotEquation> equations = new ArrayList<>();

    // 🔥 ল্যাগ কমানোর জন্য ফাস্ট রেন্ডার ফ্লাগ
    // স্লাইডার টানার সময় এটি true করে দিলে গ্রাফ দ্রুত রেন্ডার হবে
    private boolean fastRenderMode = false;

    public void setEquations(List<PlotEquation> eqs) {
        equations.clear();
        if (eqs != null) equations.addAll(eqs);
        draw();
    }
    public void setEquationsFast(List<PlotEquation> eqs) {
        fastRenderMode = true;
        equations.clear();
        if (eqs != null) equations.addAll(eqs);
        draw();
        fastRenderMode = false; // আঁকা শেষ হলে আবার নরমাল মোড
    }

    // ---------- View params ----------
    private double xCenter = 0;
    private double yCenter = 0;
    private double unitPx = 50;

    private final int minorStep = 1;
    private final int majorStep = 5;

    public GraphCanvas() {
        setWidth(800);
        setHeight(600);

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

    // ---------- World <-> Pixel ----------
    private double wxToPx(double x) {
        return (getWidth() / 2.0) + (x - xCenter) * unitPx;
    }

    private double wyToPy(double y) {
        return (getHeight() / 2.0) - (y - yCenter) * unitPx;
    }

    private void drawWorldLine(GraphicsContext g, double x1, double y1, double x2, double y2) {
        g.strokeLine(wxToPx(x1), wyToPy(y1), wxToPx(x2), wyToPy(y2));
    }

    private double interp(double a, double b, double fa, double fb) {
        double denom = (fa - fb);
        if (denom == 0) return (a + b) / 2.0;
        return a + (fa / denom) * (b - a);
    }

    // ---------- Helpers for labels ----------
    private int niceStep(double units) {
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

    // ---------- Main draw ----------
    public void draw() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext g = getGraphicsContext2D();

        // স্মুথ লাইনের জন্য
        g.setLineCap(StrokeLineCap.ROUND);
        g.setLineJoin(StrokeLineJoin.ROUND);

        // background
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        double halfWUnits = (w / 2.0) / unitPx;
        double halfHUnits = (h / 2.0) / unitPx;

        int xMin = (int) Math.floor(xCenter - halfWUnits);
        int xMax = (int) Math.ceil(xCenter + halfWUnits);
        int yMin = (int) Math.floor(yCenter - halfHUnits);
        int yMax = (int) Math.ceil(yCenter + halfHUnits);

        // -------- Minor grid --------
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

        // -------- Major grid --------
        g.setStroke(Color.web("#c9c9c9"));
        g.setLineWidth(1.4);

        int firstMajorX = (int) (Math.ceil(xMin / (double) majorStep) * majorStep);
        int firstMajorY = (int) (Math.ceil(yMin / (double) majorStep) * majorStep);

        for (int x = firstMajorX; x <= xMax; x += majorStep) {
            double px = wxToPx(x);
            g.strokeLine(px, 0, px, h);
        }
        for (int y = firstMajorY; y <= yMax; y += majorStep) {
            double py = wyToPy(y);
            g.strokeLine(0, py, w, py);
        }

        // -------- Axes --------
        g.setStroke(Color.web("#666666"));
        g.setLineWidth(2.6);

        double xAxisY = wyToPy(0);
        double yAxisX = wxToPx(0);

        g.strokeLine(0, xAxisY, w, xAxisY);
        g.strokeLine(yAxisX, 0, yAxisX, h);

        // -------- Labels --------
        g.setFill(Color.web("#444444"));
        g.setFont(Font.font(12));

        double xVisMin = xCenter - halfWUnits;
        double xVisMax = xCenter + halfWUnits;
        double yVisMin = yCenter - halfHUnits;
        double yVisMax = yCenter + halfHUnits;

        int xStep = niceStep(xVisMax - xVisMin);
        int yStep = niceStep(yVisMax - yVisMin);

        int xStart = floorToStep(xVisMin, xStep);
        int xEnd = ceilToStep(xVisMax, xStep);
        int yStart = floorToStep(yVisMin, yStep);
        int yEnd = ceilToStep(yVisMax, yStep);

        for (int x = xStart; x <= xEnd; x += xStep) {
            double px = wxToPx(x);
            if (px >= 0 && px <= w) g.fillText(String.valueOf(x), px + 2, xAxisY + 14);
        }
        for (int y = yStart; y <= yEnd; y += yStep) {
            if (y == 0) continue;
            double py = wyToPy(y);
            if (py >= 0 && py <= h) g.fillText(String.valueOf(y), yAxisX + 6, py - 2);
        }

        // -------- Plot equations --------
        g.setLineWidth(2.2);
        for (PlotEquation eq : equations) {
            if (eq == null || !eq.isVisible()) continue;

            g.setStroke(eq.getColor() == null ? Color.BLUE : eq.getColor());

            if (!eq.isImplicit()) {
                plotExplicit(g, eq, halfWUnits);
            } else {
                plotImplicitSmooth(g, eq, halfWUnits, halfHUnits);
            }
        }
    }

    // ---------- Explicit plot (Optimized) ----------
    private void plotExplicit(GraphicsContext g, PlotEquation eq, double halfWUnits) {
        double xMinPlot = xCenter - halfWUnits;
        double xMaxPlot = xCenter + halfWUnits;

        // 🔥 স্লাইডার টানলে গ্রাফ দ্রুত আঁকার জন্য স্টেপ সাইজ ডাবল করে দেওয়া হলো
        double step = Math.max(1.0 / 140.0, 1.0 / (unitPx * 2.0));
        if (fastRenderMode) step *= 2.0; // Fast mode e resolution kom hobe

        boolean started = false;
        double prevX = 0, prevY = 0;

        g.beginPath();
        for (double x = xMinPlot; x <= xMaxPlot; x += step) {
            double y = eq.evalExplicit(x);

            if (bad(y)) {
                if (started) {
                    g.stroke();
                    started = false;
                }
                continue;
            }

            if (!started) {
                g.beginPath();
                g.moveTo(wxToPx(x), wyToPy(y));
                started = true;
            } else {
                // Asymptote protection
                if (Math.abs(y - prevY) > (halfWUnits * 4)) {
                    g.stroke();
                    g.beginPath();
                    g.moveTo(wxToPx(x), wyToPy(y));
                } else {
                    g.lineTo(wxToPx(x), wyToPy(y));
                }
            }
            prevX = x;
            prevY = y;
        }
        if (started) g.stroke();
    }

    // ---------- Implicit plot (Optimized Marching Squares) ----------
    private void plotImplicitSmooth(GraphicsContext g, PlotEquation eq, double halfWUnits, double halfHUnits) {

        double xMinPlot = xCenter - halfWUnits;
        double xMaxPlot = xCenter + halfWUnits;
        double yMinPlot = yCenter - halfHUnits;
        double yMaxPlot = yCenter + halfHUnits;

        // 🔥 ল্যাগ কমানোর মেইন জায়গা:
        // নরমাল টাইমে ডিটেইলস ভালো থাকবে, কিন্তু স্লাইডার টানার সময় রেজুলেশন একটু কমে যাবে।
        double step = 15.0 / unitPx;
        step = Math.max(0.02, Math.min(step, 0.15));

        if (fastRenderMode) {
            step *= 1.8; // স্লাইডার টানলে প্রায় দ্বিগুণ স্পিডে রেন্ডার হবে
        }

        // Cache the row values to avoid recalculating the same points twice
        int xSteps = (int) Math.ceil((xMaxPlot - xMinPlot) / step) + 1;
        int ySteps = (int) Math.ceil((yMaxPlot - yMinPlot) / step) + 1;

        double[][] grid = new double[xSteps][ySteps];

        // Pre-calculate grid
        for (int i = 0; i < xSteps; i++) {
            double x = xMinPlot + i * step;
            for (int j = 0; j < ySteps; j++) {
                double y = yMinPlot + j * step;
                grid[i][j] = eq.evalImplicit(x, y);
            }
        }

        // Draw from cache
        for (int i = 0; i < xSteps - 1; i++) {
            for (int j = 0; j < ySteps - 1; j++) {

                double f00 = grid[i][j];
                double f10 = grid[i+1][j];
                double f01 = grid[i][j+1];
                double f11 = grid[i+1][j+1];

                if (bad(f00) || bad(f10) || bad(f11) || bad(f01)) continue;

                double x0 = xMinPlot + i * step;
                double x1 = x0 + step;
                double y0 = yMinPlot + j * step;
                double y1 = y0 + step;

                int count = 0;
                double[] p0 = null, p1 = null, p2 = null, p3 = null;

                if ((f00 > 0) != (f10 > 0)) {
                    p0 = new double[]{interp(x0, x1, f00, f10), y0}; count++;
                }
                if ((f10 > 0) != (f11 > 0)) {
                    p1 = new double[]{x1, interp(y0, y1, f10, f11)}; count++;
                }
                if ((f01 > 0) != (f11 > 0)) {
                    p2 = new double[]{interp(x0, x1, f01, f11), y1}; count++;
                }
                if ((f00 > 0) != (f01 > 0)) {
                    p3 = new double[]{x0, interp(y0, y1, f00, f01)}; count++;
                }

                if (count == 2) {
                    double[] a = firstNonNull(p0, p1, p2, p3);
                    double[] b = secondNonNull(p0, p1, p2, p3);
                    drawWorldLine(g, a[0], a[1], b[0], b[1]);
                } else if (count == 4) {
                    double fc = eq.evalImplicit((x0 + x1) / 2.0, (y0 + y1) / 2.0);
                    if (bad(fc)) continue;

                    if ((fc > 0) == (f00 > 0)) {
                        drawWorldLine(g, p0[0], p0[1], p3[0], p3[1]);
                        drawWorldLine(g, p2[0], p2[1], p1[0], p1[1]);
                    } else {
                        drawWorldLine(g, p0[0], p0[1], p1[0], p1[1]);
                        drawWorldLine(g, p2[0], p2[1], p3[0], p3[1]);
                    }
                }
            }
        }
    }
    private double[] firstNonNull(double[]... arr) {
        for (double[] a : arr) if (a != null) return a;
        return null;
    }

    private double[] secondNonNull(double[]... arr) {
        boolean found = false;
        for (double[] a : arr) {
            if (a == null) continue;
            if (!found) { found = true; continue; }
            return a;
        }
        return null;
    }

    // 🔥 Math checks optimized
    private boolean bad(double v) {
        // Double.isNaN is slightly slower than primitive checks in tight loops
        return v != v || v == Double.POSITIVE_INFINITY || v == Double.NEGATIVE_INFINITY;
    }

    public void setUnitPx(double unitPx) {
        this.unitPx = Math.max(10, Math.min(unitPx, 300));
        draw();
    }

    public double getUnitPx() { return unitPx; }

    public void setCenter(double x, double y) {
        this.xCenter = x;
        this.yCenter = y;
        draw();
    }
}