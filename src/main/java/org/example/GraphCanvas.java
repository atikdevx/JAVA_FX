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
        // safe initial size
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
        double t = fa / (fa - fb);
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

        // smoother looking strokes
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

        // -------- Major grid (5x5) (aligned to 0) --------
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

        // -------- Labels (dynamic) --------
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
            if (px >= 0 && px <= w) {
                g.fillText(String.valueOf(x), px + 2, xAxisY + 14);
            }
        }

        for (int y = yStart; y <= yEnd; y += yStep) {
            if (y == 0) continue;
            double py = wyToPy(y);
            if (py >= 0 && py <= h) {
                g.fillText(String.valueOf(y), yAxisX + 6, py - 2);
            }
        }

        // -------- Plot equations --------
        for (PlotEquation eq : equations) {
            if (eq == null || !eq.isVisible()) continue;

            g.setStroke(eq.getColor() == null ? Color.BLUE : eq.getColor());
            g.setLineWidth(2);

            if (!eq.isImplicit()) {
                plotExplicit(g, eq, halfWUnits);
            } else {
                plotImplicitSmooth(g, eq, halfWUnits, halfHUnits);
            }
        }
    }

    // ---------- Explicit plot (y = f(x)) ----------
    private void plotExplicit(GraphicsContext g, PlotEquation eq, double halfWUnits) {

        double xMinPlot = xCenter - halfWUnits;
        double xMaxPlot = xCenter + halfWUnits;

        // sample step based on zoom: more zoom -> smaller step
        double step = 1.0 / 60.0;

        boolean started = false;
        double prevX = 0, prevY = 0;

        for (double x = xMinPlot; x <= xMaxPlot; x += step) {

            double y = eq.evalExplicit(x);
            if (Double.isNaN(y) || Double.isInfinite(y)) {
                started = false;
                continue;
            }

            if (!started) {
                started = true;
                prevX = x;
                prevY = y;
                continue;
            }

            drawWorldLine(g, prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }
    }

    // ---------- Implicit plot (F(x,y)=0) - smooth lines ----------
    private void plotImplicitSmooth(GraphicsContext g, PlotEquation eq, double halfWUnits, double halfHUnits) {

        double xMinPlot = xCenter - halfWUnits;
        double xMaxPlot = xCenter + halfWUnits;
        double yMinPlot = yCenter - halfHUnits;
        double yMaxPlot = yCenter + halfHUnits;

        // smaller => smoother but heavier
        double step = 0.12;

        for (double x = xMinPlot; x <= xMaxPlot; x += step) {
            for (double y = yMinPlot; y <= yMaxPlot; y += step) {

                double x0 = x;
                double x1 = x + step;
                double y0 = y;
                double y1 = y + step;

                double f00 = eq.evalImplicit(x0, y0);
                double f10 = eq.evalImplicit(x1, y0);
                double f11 = eq.evalImplicit(x1, y1);
                double f01 = eq.evalImplicit(x0, y1);

                if (bad(f00) || bad(f10) || bad(f11) || bad(f01)) continue;

                // find edge crossings
                List<double[]> pts = new ArrayList<>(4);

                // bottom (x0,y0) -> (x1,y0)
                if ((f00 > 0) != (f10 > 0)) {
                    double xi = interp(x0, x1, f00, f10);
                    pts.add(new double[]{xi, y0});
                }
                // right (x1,y0) -> (x1,y1)
                if ((f10 > 0) != (f11 > 0)) {
                    double yi = interp(y0, y1, f10, f11);
                    pts.add(new double[]{x1, yi});
                }
                // top (x0,y1) -> (x1,y1)
                if ((f01 > 0) != (f11 > 0)) {
                    double xi = interp(x0, x1, f01, f11);
                    pts.add(new double[]{xi, y1});
                }
                // left (x0,y0) -> (x0,y1)
                if ((f00 > 0) != (f01 > 0)) {
                    double yi = interp(y0, y1, f00, f01);
                    pts.add(new double[]{x0, yi});
                }

                // draw segments
                if (pts.size() == 2) {
                    drawWorldLine(g, pts.get(0)[0], pts.get(0)[1], pts.get(1)[0], pts.get(1)[1]);
                } else if (pts.size() == 4) {
                    // simple pairing
                    drawWorldLine(g, pts.get(0)[0], pts.get(0)[1], pts.get(1)[0], pts.get(1)[1]);
                    drawWorldLine(g, pts.get(2)[0], pts.get(2)[1], pts.get(3)[0], pts.get(3)[1]);
                }
            }
        }
    }

    private boolean bad(double v) {
        return Double.isNaN(v) || Double.isInfinite(v);
    }
}

//package com.equationplotter.ui;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.paint.Color;
//import javafx.scene.text.Font;
//import java.util.ArrayList;
//import java.util.List;
//
//public class GraphCanvas extends Canvas {
//    private final List<PlotEquation> equations = new ArrayList<>();
//
//    public void setEquations(List<PlotEquation> eqs) {
//        equations.clear();
//        if (eqs != null) equations.addAll(eqs);
//        draw();
//    }
//    private double xCenter = 0;
//    private double yCenter = 0;
//
//    // 1 unit = কত px (zoom later)
//    private double unitPx = 50;
//
//    private final int minorStep = 1;
//    private final int majorStep = 5;
//    private int niceStep(double units) {
//        // units = about how many numbers visible on axis
//        // returns a "nice" step: 1,2,5,10,20...
//        if (units <= 10) return 1;
//        if (units <= 20) return 2;
//        if (units <= 50) return 5;
//        if (units <= 100) return 10;
//        if (units <= 200) return 20;
//        return 50;
//    }
//
//    private int floorToStep(double v, int step) {
//        return (int) Math.floor(v / step) * step;
//    }
//
//    private int ceilToStep(double v, int step) {
//        return (int) Math.ceil(v / step) * step;
//    }
//    public GraphCanvas() {
//        // initial safe size (0 হলে draw skip হয়, তাই পরে parent থেকে set হবে)
//        setWidth(800);
//        setHeight(600);
//
//        // redraw when width/height changes
//        widthProperty().addListener((o, a, b) -> draw());
//        heightProperty().addListener((o, a, b) -> draw());
//
//        draw();
//    }
//
//    @Override
//    public boolean isResizable() { return true; }
//
//    @Override
//    public void resize(double w, double h) {
//        setWidth(w);
//        setHeight(h);
//        draw();
//    }
//
//    private double wxToPx(double x) {
//        double cx = getWidth() / 2.0;
//        return cx + (x - xCenter) * unitPx;
//    }
//
//    private double wyToPy(double y) {
//        double cy = getHeight() / 2.0;
//        return cy - (y - yCenter) * unitPx;
//    }
//
//    public void draw() {
//        double w = getWidth();
//        double h = getHeight();
//        if (w <= 0 || h <= 0) return;
//
//        GraphicsContext g = getGraphicsContext2D();
//
//        // background
//        g.setFill(Color.WHITE);
//        g.fillRect(0, 0, w, h);
//
//        // visible world range (depends on window size + unitPx)
//        double halfWUnits = (w / 2.0) / unitPx;
//        double halfHUnits = (h / 2.0) / unitPx;
//
//        int xMin = (int) Math.floor(xCenter - halfWUnits);
//        int xMax = (int) Math.ceil (xCenter + halfWUnits);
//        int yMin = (int) Math.floor(yCenter - halfHUnits);
//        int yMax = (int) Math.ceil (yCenter + halfHUnits);
//
//        // -------- Minor grid (1x1) --------
//        g.setStroke(Color.web("#eeeeee"));
//        g.setLineWidth(1);
//
//        for (int x = xMin; x <= xMax; x += minorStep) {
//            double px = wxToPx(x);
//            g.strokeLine(px, 0, px, h);
//        }
//        for (int y = yMin; y <= yMax; y += minorStep) {
//            double py = wyToPy(y);
//            g.strokeLine(0, py, w, py);
//        }
//
//        // -------- Major grid (5x5) – MUST start from (0,0) --------
//        g.setStroke(Color.web("#c9c9c9"));
//        g.setLineWidth(1.4);
//
//        int firstMajorX = (int) (Math.ceil(xMin / (double)majorStep) * majorStep);
//        int firstMajorY = (int) (Math.ceil(yMin / (double)majorStep) * majorStep);
//
//        for (int x = firstMajorX; x <= xMax; x += majorStep) {
//            double px = wxToPx(x);
//            g.strokeLine(px, 0, px, h);
//        }
//        for (int y = firstMajorY; y <= yMax; y += majorStep) {
//            double py = wyToPy(y);
//            g.strokeLine(0, py, w, py);
//        }
//
//        // -------- Axes (superimposed exactly on grid lines) --------
//        g.setStroke(Color.web("#666666"));
//        g.setLineWidth(2.6);
//
//        double xAxisY = wyToPy(0);
//        double yAxisX = wxToPx(0);
//
//        g.strokeLine(0, xAxisY, w, xAxisY);   // X-axis
//        g.strokeLine(yAxisX, 0, yAxisX, h);   // Y-axis
//
//        // -------- Numbering: like Desmos (major ticks only) --------
//        // -------- Dynamic Numbering (fills whole screen) --------
//        g.setFill(Color.web("#444444"));
//        g.setFont(Font.font(12));
//
//// visible world range based on window size
//        double xVisMin = xCenter - halfWUnits;
//        double xVisMax = xCenter + halfWUnits;
//
//        double yVisMin = yCenter - halfHUnits;
//        double yVisMax = yCenter + halfHUnits;
//
//// choose label step so it doesn't become too crowded
//        int xStep = niceStep(xVisMax - xVisMin);
//        int yStep = niceStep(yVisMax - yVisMin);
//
//// start/end aligned to step
//        int xStart = floorToStep(xVisMin, xStep);
//        int xEnd   = ceilToStep(xVisMax, xStep);
//
//        int yStart = floorToStep(yVisMin, yStep);
//        int yEnd   = ceilToStep(yVisMax, yStep);
//
//// X-axis labels (left to right)
//        for (int x = xStart; x <= xEnd; x += xStep) {
//            double px = wxToPx(x);
//            // show label only if within screen
//            if (px >= 0 && px <= w) {
//                g.fillText(String.valueOf(x), px + 2, xAxisY + 14);
//            }
//        }
//
//// Y-axis labels (top to bottom)
//        for (int y = yStart; y <= yEnd; y += yStep) {
//            if (y == 0) continue; // 0 বাদ দিলে clean দেখায়
//            double py = wyToPy(y);
//            if (py >= 0 && py <= h) {
//                g.fillText(String.valueOf(y), yAxisX + 6, py - 2);
//            }
//        }
//        // x labels on x-axis (every 2 or 5; you want -10..10 style, so do 2)
//        // -------- Plot equations --------
//        // -------- Plot equations --------
//        for (PlotEquation eq : equations) {
//
//            if (eq == null || !eq.isVisible()) continue;
//
//            g.setStroke(eq.getColor());
//            g.setLineWidth(2);
//
//            // ---------- EXPLICIT ----------
//            if (!eq.isImplicit()) {
//
//                boolean started = false;
//                double prevPx = 0, prevPy = 0;
//
//                double xMinPlot = xCenter - halfWUnits;
//                double xMaxPlot = xCenter + halfWUnits;
//
//                double step = 1.0 / 40.0;
//
//                for (double x = xMinPlot; x <= xMaxPlot; x += step) {
//
//                    double y = eq.evalExplicit(x);
//                    if (Double.isNaN(y) || Double.isInfinite(y)) {
//                        started = false;
//                        continue;
//                    }
//
//                    double px = wxToPx(x);
//                    double py = wyToPy(y);
//
//                    if (started) g.strokeLine(prevPx, prevPy, px, py);
//                    else started = true;
//
//                    prevPx = px;
//                    prevPy = py;
//                }
//            }
//
//            // ---------- IMPLICIT ----------
//            else {
//
//                double xMinPlot = xCenter - halfWUnits;
//                double xMaxPlot = xCenter + halfWUnits;
//                double yMinPlot = yCenter - halfHUnits;
//                double yMaxPlot = yCenter + halfHUnits;
//
//                double sample = 0.08; // resolution
//
//                for (double x = xMinPlot; x <= xMaxPlot; x += sample) {
//                    for (double y = yMinPlot; y <= yMaxPlot; y += sample) {
//
//                        double v = eq.evalImplicit(x, y);
//
//                        if (Math.abs(v) < 0.02) { // near zero → draw pixel
//                            double px = wxToPx(x);
//                            double py = wyToPy(y);
//                            g.strokeLine(px, py, px, py);
//                        }
//                    }
//                }
//            }
//        }
//    }
//}