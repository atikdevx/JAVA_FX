package com.equationplotter.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class GraphCanvas extends Canvas {

    // ---------- Equations ----------
    private final List<PlotEquation> equations = new ArrayList<>();
    private boolean fastRenderMode = false;
    private boolean isDarkMode = false;

    public void setEquations(List<PlotEquation> eqs) {
        equations.clear();
        if (eqs != null) equations.addAll(eqs);
        draw();
    }

    public void setDarkMode(boolean isDark) {
        this.isDarkMode = isDark;
        draw();
    }

    public void setEquationsFast(List<PlotEquation> eqs) {
        fastRenderMode = true;
        equations.clear();
        if (eqs != null) equations.addAll(eqs);
        draw();
        fastRenderMode = false;
    }

    // ---------- View params ----------
    private double xCenter = 0;
    private double yCenter = 0;
    private double unitPx = 50; // Pixels per unit

    // Panning variables
    private double lastMouseX;
    private double lastMouseY;

    public GraphCanvas() {
        setWidth(800);
        setHeight(600);

        widthProperty().addListener((o, a, b) -> draw());
        heightProperty().addListener((o, a, b) -> draw());

        // 1. Zoom on Scroll
        this.setOnScroll((ScrollEvent event) -> {
            if (event.getDeltaY() == 0) return;
            double zoomFactor = 1.05;
            double scale = (event.getDeltaY() > 0) ? zoomFactor : (1 / zoomFactor);
            double mouseWx = pxToWx(event.getX());
            double mouseWy = pxToWy(event.getY());
            unitPx *= scale;
            if (unitPx < 2) unitPx = 2;
            if (unitPx > 10000) unitPx = 10000;
            double halfW = getWidth() / 2.0;
            double halfH = getHeight() / 2.0;
            xCenter = mouseWx - (event.getX() - halfW) / unitPx;
            yCenter = mouseWy + (event.getY() - halfH) / unitPx;
            draw();
            event.consume();
        });

        // 2. Pan (Drag) Start
        this.setOnMousePressed((MouseEvent event) -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        // 3. Pan (Drag) Move
        this.setOnMouseDragged((MouseEvent event) -> {
            double dx = event.getX() - lastMouseX;
            double dy = event.getY() - lastMouseY;
            xCenter -= dx / unitPx;
            yCenter += dy / unitPx;
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            draw();
        });

        draw();
    }

    public void resetView() {
        this.xCenter = 0;
        this.yCenter = 0;
        this.unitPx = 50;
        draw();
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double w, double h) {
        setWidth(w);
        setHeight(h);
        draw();
    }

    // ---------- World <-> Pixel ----------
    private double wxToPx(double x) { return (getWidth() / 2.0) + (x - xCenter) * unitPx; }
    private double wyToPy(double y) { return (getHeight() / 2.0) - (y - yCenter) * unitPx; }
    private double pxToWx(double px) { return (px - getWidth() / 2.0) / unitPx + xCenter; }
    private double pxToWy(double py) { return (getHeight() / 2.0 - py) / unitPx + yCenter; }

    private void drawWorldLine(GraphicsContext g, double x1, double y1, double x2, double y2) {
        g.strokeLine(wxToPx(x1), wyToPy(y1), wxToPx(x2), wyToPy(y2));
    }

    private double interp(double a, double b, double fa, double fb) {
        double denom = (fa - fb);
        if (denom == 0) return (a + b) / 2.0;
        return a + (fa / denom) * (b - a);
    }

    private double calculateGridStep() {
        double targetPixelSpacing = 60.0;
        double rawStep = targetPixelSpacing / unitPx;
        double exponent = Math.floor(Math.log10(rawStep));
        double powerOf10 = Math.pow(10, exponent);
        double normalized = rawStep / powerOf10;
        double step;
        if (normalized < 2) step = 1;
        else if (normalized < 5) step = 2;
        else step = 5;
        return step * powerOf10;
    }

    // ---------- Main draw ----------
    public void draw() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext g = getGraphicsContext2D();

        Color bgColor = isDarkMode ? Color.web("#121212") : Color.WHITE;
        Color minorGridColor = isDarkMode ? Color.web("#333333") : Color.web("#eeeeee");
        Color majorGridColor = isDarkMode ? Color.web("#555555") : Color.web("#c9c9c9");
        Color axisColor = isDarkMode ? Color.web("#aaaaaa") : Color.web("#666666");
        Color textColor = isDarkMode ? Color.web("#dddddd") : Color.web("#444444");

        g.setFill(bgColor);
        g.fillRect(0, 0, w, h);

        g.setLineCap(StrokeLineCap.ROUND);
        g.setLineJoin(StrokeLineJoin.ROUND);

        double halfWUnits = (w / 2.0) / unitPx;
        double halfHUnits = (h / 2.0) / unitPx;

        double xVisMin = xCenter - halfWUnits;
        double xVisMax = xCenter + halfWUnits;
        double yVisMin = yCenter - halfHUnits;
        double yVisMax = yCenter + halfHUnits;

        double minorStep = calculateGridStep();
        double majorStep = minorStep * 5.0;

        g.setStroke(minorGridColor);
        g.setLineWidth(1);
        drawGridLines(g, minorStep, xVisMin, xVisMax, yVisMin, yVisMax, w, h);

        g.setStroke(majorGridColor);
        g.setLineWidth(1.4);
        drawGridLines(g, majorStep, xVisMin, xVisMax, yVisMin, yVisMax, w, h);

        g.setStroke(axisColor);
        g.setLineWidth(2.6);
        double xAxisY = wyToPy(0);
        double yAxisX = wxToPx(0);
        if (xAxisY >= 0 && xAxisY <= h) g.strokeLine(0, xAxisY, w, xAxisY);
        if (yAxisX >= 0 && yAxisX <= w) g.strokeLine(yAxisX, 0, yAxisX, h);

        g.setFill(textColor);
        g.setFont(Font.font("Arial", 12));

        long startX = (long) Math.ceil(xVisMin / majorStep);
        long endX = (long) Math.floor(xVisMax / majorStep);
        long startY = (long) Math.ceil(yVisMin / majorStep);
        long endY = (long) Math.floor(yVisMax / majorStep);

        for (long i = startX; i <= endX; i++) {
            double val = i * majorStep;
            if (Math.abs(val) < 1e-9) continue;
            g.fillText(formatNumber(val), wxToPx(val) - 4, xAxisY + 16);
        }

        for (long i = startY; i <= endY; i++) {
            double val = i * majorStep;
            if (Math.abs(val) < 1e-9) continue;
            g.fillText(formatNumber(val), yAxisX + 8, wyToPy(val) + 4);
        }
        g.fillText("0", yAxisX - 12, xAxisY + 16);

        // Draw Equations
        g.setLineWidth(2.2);
        int pointCounter = 1;

        for (PlotEquation eq : equations) {
            if (eq == null || !eq.isVisible()) continue;
            g.setStroke(eq.getColor() == null ? Color.BLUE : eq.getColor());
            g.setFill(eq.getColor() == null ? Color.BLUE : eq.getColor());

            if (eq.isPoint()) {
                plotPoint(g, eq, pointCounter++);
            } else if (!eq.isImplicit()) {
                plotExplicit(g, eq, halfWUnits, halfHUnits);
            } else {
                plotImplicitSmooth(g, eq, halfWUnits, halfHUnits);
            }
        }
    }

    private void drawGridLines(GraphicsContext g, double step, double xMin, double xMax, double yMin, double yMax, double w, double h) {
        double firstX = Math.ceil(xMin / step) * step;
        for (double x = firstX; x <= xMax; x += step) {
            double px = wxToPx(x);
            g.strokeLine(px, 0, px, h);
        }
        double firstY = Math.ceil(yMin / step) * step;
        for (double y = firstY; y <= yMax; y += step) {
            double py = wyToPy(y);
            g.strokeLine(0, py, w, py);
        }
    }

    private String formatNumber(double val) {
        if (Math.abs(val - Math.round(val)) < 1e-9) return String.valueOf((long) Math.round(val));
        return String.format("%.2f", val);
    }

    // ---------- Points ----------
    private void plotPoint(GraphicsContext g, PlotEquation eq, int index) {
        double px = eq.getPointX();
        double py = eq.getPointY();
        if (bad(px) || bad(py)) return;

        double screenX = wxToPx(px);
        double screenY = wyToPy(py);
        double r = 5.0;

        g.fillOval(screenX - r, screenY - r, r * 2, r * 2);

        if (eq.isLabelVisible()) {
            g.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            g.setFill(isDarkMode ? Color.WHITE : Color.BLACK);
            g.fillText(eq.getDynamicLabel(index), screenX + 8, screenY - 8);
        }
    }

    // ---------- Explicit plot (Cleaned up) ----------
    private void plotExplicit(GraphicsContext g, PlotEquation eq, double halfWUnits, double halfHUnits) {
        double xMinPlot = xCenter - halfWUnits;
        double xMaxPlot = xCenter + halfWUnits;
        double yMinPlot = yCenter - halfHUnits;
        double yMaxPlot = yCenter + halfHUnits;

        double screenHeight = yMaxPlot - yMinPlot;
        double overdraw = screenHeight * 0.1;

        double step = 1.0 / unitPx;
        long totalSteps = (long) Math.ceil((xMaxPlot - xMinPlot) / step);

        boolean penDown = false;
        double prevY = Double.NaN;
        double prevX = Double.NaN;

        for (long i = 0; i <= totalSteps; i++) {
            double x = xMinPlot + (i * step);
            double y = eq.evalExplicit(x);

            // ---- 1. Domain Boundary (Valid -> NaN) ----
            if (bad(y)) {
                if (penDown) {
                    double edgeX = findBoundary(eq, prevX, x, true);
                    double edgeY = eq.evalExplicit(edgeX);

                    if (!bad(edgeY)) {
                        double clampedEdgeY = Math.max(yMinPlot - overdraw, Math.min(yMaxPlot + overdraw, edgeY));
                        g.lineTo(wxToPx(edgeX), wyToPy(clampedEdgeY));
                    }
                    g.stroke();
                    penDown = false;
                }
                prevX = x;
                prevY = y;
                continue;
            }

            double clampedY = Math.max(yMinPlot - overdraw, Math.min(yMaxPlot + overdraw, y));

            // ---- 2. Entering Domain (NaN -> Valid) ----
            if (!penDown) {
                g.beginPath();
                if (bad(prevY) && !Double.isNaN(prevX)) {
                    double edgeX = findBoundary(eq, prevX, x, false);
                    double edgeY = eq.evalExplicit(edgeX);

                    if (!bad(edgeY)) {
                        double clampedEdgeY = Math.max(yMinPlot - overdraw, Math.min(yMaxPlot + overdraw, edgeY));
                        g.moveTo(wxToPx(edgeX), wyToPy(clampedEdgeY));
                        g.lineTo(wxToPx(x), wyToPy(clampedY));
                    } else {
                        g.moveTo(wxToPx(x), wyToPy(clampedY));
                    }
                } else {
                    g.moveTo(wxToPx(x), wyToPy(clampedY));
                }
                penDown = true;
            } else {

                // ---- 3. Vertical Asymptotes & Step Functions (Valid -> Valid) ----
                boolean breakLine = false;
                double dy = Math.abs(y - prevY);
                double pixelDy = dy * unitPx;

                if (pixelDy > 2.0) {
                    double midX = (prevX + x) / 2.0;
                    double midY = eq.evalExplicit(midX);

                    if (bad(midY)) {
                        breakLine = true;
                    } else {
                        double expectedMidY = (prevY + y) / 2.0;
                        double deviation = Math.abs(midY - expectedMidY);

                        if (deviation >= dy * 0.3) {
                            // High deviation means it's an asymptote (tan(x), 1/x)
                            breakLine = true;
                        } else if (Math.abs(midY - prevY) < 1e-7 || Math.abs(midY - y) < 1e-7) {
                            // Perfect flat step (floor(x), ceil(x))
                            breakLine = true;
                        } else if (dy > screenHeight * 0.5 && (y * prevY < 0)) {
                            // Massive jump crossing zero
                            breakLine = true;
                        } else if (dy > screenHeight * 2.0) {
                            // Just a massive jump off-screen
                            breakLine = true;
                        }
                    }
                }

                if (breakLine) {
                    // Break the line and move pen, rather than drawing false asymptote lines
                    g.stroke();
                    g.beginPath();
                    g.moveTo(wxToPx(x), wyToPy(clampedY));
                } else {
                    g.lineTo(wxToPx(x), wyToPy(clampedY));
                }
            }

            prevX = x;
            prevY = y;
        }

        if (penDown) g.stroke();
    }

    private double findBoundary(PlotEquation eq, double left, double right, boolean leftIsValid) {
        double validX = leftIsValid ? left : right;
        for (int i = 0; i < 20; i++) {
            double mid = (left + right) / 2.0;
            double midY = eq.evalExplicit(mid);
            boolean midIsBad = bad(midY);

            if (leftIsValid) {
                if (midIsBad) right = mid;
                else { left = mid; validX = mid; }
            } else {
                if (midIsBad) left = mid;
                else { right = mid; validX = mid; }
            }
        }
        return validX;
    }

    // ---------- Implicit plot ----------
    private void plotImplicitSmooth(GraphicsContext g, PlotEquation eq, double halfWUnits, double halfHUnits) {
        double xMinPlot = xCenter - halfWUnits;
        double xMaxPlot = xCenter + halfWUnits;
        double yMinPlot = yCenter - halfHUnits;
        double yMaxPlot = yCenter + halfHUnits;
        double step = 4 / unitPx;
        if (fastRenderMode) step *= 1.8;

        int xSteps = (int) Math.ceil((xMaxPlot - xMinPlot) / step) + 1;
        int ySteps = (int) Math.ceil((yMaxPlot - yMinPlot) / step) + 1;
        if (xSteps > 600) xSteps = 600;
        if (ySteps > 600) ySteps = 600;

        double[][] grid = new double[xSteps][ySteps];
        for (int i = 0; i < xSteps; i++) {
            double x = xMinPlot + i * step;
            for (int j = 0; j < ySteps; j++) {
                double y = yMinPlot + j * step;
                grid[i][j] = eq.evalImplicit(x, y);
            }
        }

        for (int i = 0; i < xSteps - 1; i++) {
            for (int j = 0; j < ySteps - 1; j++) {
                double f00 = grid[i][j];
                double f10 = grid[i + 1][j];
                double f01 = grid[i][j + 1];
                double f11 = grid[i + 1][j + 1];

                if (bad(f00) || bad(f10) || bad(f11) || bad(f01)) continue;

                double x0 = xMinPlot + i * step;
                double x1 = x0 + step;
                double y0 = yMinPlot + j * step;
                double y1 = y0 + step;

                int count = 0;
                double[] p0 = null, p1 = null, p2 = null, p3 = null;
                if ((f00 > 0) != (f10 > 0)) { p0 = new double[]{interp(x0, x1, f00, f10), y0}; count++; }
                if ((f10 > 0) != (f11 > 0)) { p1 = new double[]{x1, interp(y0, y1, f10, f11)}; count++; }
                if ((f01 > 0) != (f11 > 0)) { p2 = new double[]{interp(x0, x1, f01, f11), y1}; count++; }
                if ((f00 > 0) != (f01 > 0)) { p3 = new double[]{x0, interp(y0, y1, f00, f01)}; count++; }

                if (count == 2) {
                    double[] a = firstNonNull(p0, p1, p2, p3);
                    double[] b = secondNonNull(p0, p1, p2, p3);
                    if (a != null && b != null) drawWorldLine(g, a[0], a[1], b[0], b[1]);
                } else if (count == 4) {
                    double fc = eq.evalImplicit((x0 + x1) / 2.0, (y0 + y1) / 2.0);
                    if (!bad(fc)) {
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
        return v != v || v == Double.POSITIVE_INFINITY || v == Double.NEGATIVE_INFINITY;
    }

    public void setCenter(double x, double y) {
        this.xCenter = x;
        this.yCenter = y;
        draw();
    }
}