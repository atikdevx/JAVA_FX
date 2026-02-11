package com.equationplotter.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class GraphCanvas extends Canvas {

    // ---------- Equations ----------
    private final List<PlotEquation> equations = new ArrayList<>();

    public void setEquations(List<PlotEquation> eqs) {
        equations.clear();
        if (eqs != null) equations.addAll(eqs);
        draw();
    }

    // ---------- View params ----------
    private double xCenter = 0;
    private double yCenter = 0;

    // 1 unit = কত px (zoom)
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
        double cx = getWidth() / 2.0;
        return cx + (x - xCenter) * unitPx;
    }

    private double wyToPy(double y) {
        double cy = getHeight() / 2.0;
        return cy - (y - yCenter) * unitPx;
    }

    private void drawWorldLine(GraphicsContext g, double x1, double y1, double x2, double y2) {
        g.strokeLine(wxToPx(x1), wyToPy(y1), wxToPx(x2), wyToPy(y2));
    }

    // linear interpolation where f crosses 0 between (a,fa) and (b,fb)
    private double interp(double a, double b, double fa, double fb) {
        double denom = (fa - fb);
        if (denom == 0) return (a + b) / 2.0;
        double t = fa / denom;
        return a + t * (b - a);
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

        g.setLineCap(StrokeLineCap.ROUND);

        // background
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        // visible world range
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
        for (PlotEquation eq : equations) {
            if (eq == null || !eq.isVisible()) continue;

            g.setStroke(eq.getColor() == null ? Color.BLUE : eq.getColor());
            g.setLineWidth(2.2);

            if (!eq.isImplicit()) {
                plotExplicit(g, eq, halfWUnits);
            } else {
                plotImplicitSmooth(g, eq, halfWUnits, halfHUnits);
            }
        }
    }

    // ---------- Explicit plot ----------
    private void plotExplicit(GraphicsContext g, PlotEquation eq, double halfWUnits) {

        double xMinPlot = xCenter - halfWUnits;
        double xMaxPlot = xCenter + halfWUnits;

        // zoom-aware step
        double step = Math.max(1.0 / 140.0, 1.0 / (unitPx * 3.0)); // clamp
        boolean started = false;
        double prevX = 0, prevY = 0;

        for (double x = xMinPlot; x <= xMaxPlot; x += step) {
            double y = eq.evalExplicit(x);
            if (bad(y)) {
                started = false;
                continue;
            }
            if (!started) {
                started = true;
                prevX = x;
                prevY = y;
                continue;
            }
            // break on huge jumps (asymptote protection)
            if (Math.abs(y - prevY) > (halfWUnits * 4)) {
                started = false;
                continue;
            }

            drawWorldLine(g, prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }
    }

    // ---------- Implicit plot (Marching Squares + asymptotic decider) ----------
    private void plotImplicitSmooth(GraphicsContext g, PlotEquation eq, double halfWUnits, double halfHUnits) {

        double xMinPlot = xCenter - halfWUnits;
        double xMaxPlot = xCenter + halfWUnits;
        double yMinPlot = yCenter - halfHUnits;
        double yMaxPlot = yCenter + halfHUnits;

        // step in world units; smaller => smoother.
        // zoom in => smaller step, zoom out => bigger step
        double step = 18.0 / unitPx;          // ~18px per cell
        step = Math.max(0.03, Math.min(step, 0.20)); // clamp for performance/smoothness

        for (double x = xMinPlot; x <= xMaxPlot; x += step) {
            for (double y = yMinPlot; y <= yMaxPlot; y += step) {

                double x0 = x, x1 = x + step;
                double y0 = y, y1 = y + step;

                double f00 = eq.evalImplicit(x0, y0);
                double f10 = eq.evalImplicit(x1, y0);
                double f11 = eq.evalImplicit(x1, y1);
                double f01 = eq.evalImplicit(x0, y1);

                if (bad(f00) || bad(f10) || bad(f11) || bad(f01)) continue;

                // collect intersections on edges in consistent order:
                // E0 bottom, E1 right, E2 top, E3 left
                double[] p0 = null, p1 = null, p2 = null, p3 = null;
                int count = 0;

                if ((f00 > 0) != (f10 > 0)) { // bottom
                    double xi = interp(x0, x1, f00, f10);
                    p0 = new double[]{xi, y0}; count++;
                }
                if ((f10 > 0) != (f11 > 0)) { // right
                    double yi = interp(y0, y1, f10, f11);
                    p1 = new double[]{x1, yi}; count++;
                }
                if ((f01 > 0) != (f11 > 0)) { // top
                    double xi = interp(x0, x1, f01, f11);
                    p2 = new double[]{xi, y1}; count++;
                }
                if ((f00 > 0) != (f01 > 0)) { // left
                    double yi = interp(y0, y1, f00, f01);
                    p3 = new double[]{x0, yi}; count++;
                }

                if (count < 2) continue;

                if (count == 2) {
                    // find the two points and connect
                    double[] a = firstNonNull(p0, p1, p2, p3);
                    double[] b = secondNonNull(p0, p1, p2, p3);
                    drawWorldLine(g, a[0], a[1], b[0], b[1]);
                    continue;
                }

                if (count == 4) {
                    // ambiguous case. Use asymptotic decider via center value.
                    double xc = (x0 + x1) / 2.0;
                    double yc = (y0 + y1) / 2.0;
                    double fc = eq.evalImplicit(xc, yc);
                    if (bad(fc)) continue;

                    // if center sign matches f00 -> connect (bottom-left) & (top-right)
                    // else connect (bottom-right) & (top-left)
                    boolean sameAs00 = (fc > 0) == (f00 > 0);

                    if (sameAs00) {
                        // connect p0(bottom) with p3(left)
                        // connect p2(top) with p1(right)
                        drawWorldLine(g, p0[0], p0[1], p3[0], p3[1]);
                        drawWorldLine(g, p2[0], p2[1], p1[0], p1[1]);
                    } else {
                        // connect p0(bottom) with p1(right)
                        // connect p2(top) with p3(left)
                        drawWorldLine(g, p0[0], p0[1], p1[0], p1[1]);
                        drawWorldLine(g, p2[0], p2[1], p3[0], p3[1]);
                    }
                }

                // count==3 theoretically rare due to exact-zero corners; ignore safely
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

    private boolean bad(double v) {
        return Double.isNaN(v) || Double.isInfinite(v);
    }

    // optional: later zoom/pan hooks
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
