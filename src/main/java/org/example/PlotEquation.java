package com.equationplotter.ui;

import javafx.scene.paint.Color;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class PlotEquation {

    private String rawText;
    private Color color;
    private boolean visible = true;

    private Expression explicitExpr;   // y = f(x)
    private Expression implicitExpr;   // f(x,y) = 0

    private boolean isImplicit = false;

    public PlotEquation(String rawText, Color color) {
        this.rawText = rawText;
        this.color = color;
        compile();
    }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) {
        this.rawText = rawText;
        compile();
    }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { visible = v; }

    public boolean isImplicit() { return isImplicit; }

    // ---------- evaluate ----------

    public double evalExplicit(double x) {
        if (explicitExpr == null) return Double.NaN;
        try {
            return explicitExpr.setVariable("x", x).evaluate();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public double evalImplicit(double x, double y) {
        if (implicitExpr == null) return Double.NaN;
        try {
            return implicitExpr
                    .setVariable("x", x)
                    .setVariable("y", y)
                    .evaluate();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    // ---------- compile ----------

    private void compile() {
        try {
            String s = rawText.trim();

            // implicit detection
            if (s.contains("=") && !s.startsWith("y=")) {
                // example: x^2 + y^2 = 1  →  x^2 + y^2 - (1)
                String[] parts = s.split("=");
                String expr = "(" + parts[0] + ")-(" + parts[1] + ")";

                implicitExpr = new ExpressionBuilder(expr)
                        .variables("x", "y")
                        .build();

                explicitExpr = null;
                isImplicit = true;
                return;
            }

            // explicit y = f(x)
            if (s.startsWith("y=")) s = s.substring(2);

            s = s.replaceAll("(\\d)(x)", "$1*$2");

            explicitExpr = new ExpressionBuilder(s)
                    .variable("x")
                    .build();

            implicitExpr = null;
            isImplicit = false;

        } catch (Exception e) {
            explicitExpr = null;
            implicitExpr = null;
        }
    }
}
