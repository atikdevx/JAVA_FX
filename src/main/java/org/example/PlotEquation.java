package com.equationplotter.ui;

import javafx.scene.paint.Color;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotEquation {

    private String rawText;
    private String normalizedText;

    private Color color;
    private boolean visible = true;

    private Expression explicitExpr;   // y = f(x)
    private Expression implicitExpr;   // F(x,y)=0

    private boolean isImplicit = false;

    // parameter values
    private final Map<String, Double> params = new HashMap<>();
    private List<String> paramNames = List.of();

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

    public List<String> getParamNames() { return paramNames; }

    public void setParam(String name, double value) {
        if (name == null) return;
        params.put(name.toLowerCase(), value);
    }

    public double getParam(String name, double defaultVal) {
        if (name == null) return defaultVal;
        return params.getOrDefault(name.toLowerCase(), defaultVal);
    }

    // ---------- evaluate ----------
    public double evalExplicit(double x) {
        if (explicitExpr == null) return Double.NaN;
        try {
            explicitExpr.setVariable("x", x);
            for (var e : params.entrySet()) {
                explicitExpr.setVariable(e.getKey(), e.getValue());
            }
            return explicitExpr.evaluate();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public double evalImplicit(double x, double y) {
        if (implicitExpr == null) return Double.NaN;
        try {
            implicitExpr.setVariable("x", x);
            implicitExpr.setVariable("y", y);
            for (var e : params.entrySet()) {
                implicitExpr.setVariable(e.getKey(), e.getValue());
            }
            return implicitExpr.evaluate();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    // ---------- compile ----------
    private void compile() {
        try {
            String s = rawText == null ? "" : rawText.trim();
            if (s.isEmpty()) {
                explicitExpr = null;
                implicitExpr = null;
                return;
            }

            // normalize (handles dot->*, implicit multiplications, etc)
            normalizedText = EquationNormalizer.normalize(s);

            // detect params from normalized form
            paramNames = ParamExtractor.extractParams(normalizedText);
            for (String p : paramNames) params.putIfAbsent(p, 1.0); // default value 1

            // IMPLICIT: has '=' but not "y="
            if (normalizedText.contains("=") && !normalizedText.startsWith("y=")) {
                String[] parts = normalizedText.split("=");
                String expr = "(" + parts[0] + ")-(" + parts[1] + ")";

                ExpressionBuilder b = new ExpressionBuilder(expr).variables("x", "y");
                for (String p : paramNames) b = b.variable(p);

                implicitExpr = b.build();

                explicitExpr = null;
                isImplicit = true;
                return;
            }

            // EXPLICIT
            String rhs = normalizedText;
            if (rhs.startsWith("y=")) rhs = rhs.substring(2);

            ExpressionBuilder b = new ExpressionBuilder(rhs).variable("x");
            for (String p : paramNames) b = b.variable(p);

            explicitExpr = b.build();

            implicitExpr = null;
            isImplicit = false;

        } catch (Exception e) {
            explicitExpr = null;
            implicitExpr = null;
        }
    }
}
