package com.equationplotter.ui;

import javafx.scene.paint.Color;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlotEquation {

    private String rawText;
    private Color color;
    private boolean visible = true;

    // Type flags
    private boolean isPoint = false;
    private boolean labelVisible = false;

    // Point Expressions (dynamic)
    private Expression pointXExpr;
    private Expression pointYExpr;

    // Equation Expressions
    private boolean implicit;
    private Expression explicitExpr;
    private Expression implicitExpr;

    private final Map<String, Double> params = new HashMap<>();
    private final List<String> paramNames = new ArrayList<>();

    // Regex to find words (potential variables)
    private static final Pattern VAR_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    // Set of known functions to ignore when extracting variables
    private static final Set<String> KNOWN_FUNCS = new HashSet<>(Arrays.asList(
            "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh",
            "sqrt", "cbrt", "abs", "ceil", "floor", "exp", "log", "log10", "log2",
            "signum", "x", "y" // x and y are plot vars, not params (usually)
    ));

    public PlotEquation(String text, Color color) {
        this.rawText = text == null ? "" : text;
        this.color = color == null ? Color.BLUE : color;
        rebuild();
    }

    public void rebuild() {
        String s = rawText.trim();
        this.isPoint = false;
        this.implicit = false;
        this.explicitExpr = null;
        this.implicitExpr = null;
        this.pointXExpr = null;
        this.pointYExpr = null;
        this.paramNames.clear();

        if (s.isEmpty()) return;

        // 1. Try parsing as a Point: ( expr , expr )
        if (s.startsWith("(") && s.endsWith(")")) {
            String content = s.substring(1, s.length() - 1);
            int splitIndex = findTopLevelComma(content);

            if (splitIndex != -1) {
                String sX = content.substring(0, splitIndex).trim();
                String sY = content.substring(splitIndex + 1).trim();

                // It is a point candidate. Let's try to build expressions.
                // For points, we treat EVERYTHING as a parameter (even 'x' or 't' if used)
                Set<String> varsX = extractVariables(sX, true);
                Set<String> varsY = extractVariables(sY, true);
                Set<String> allVars = new TreeSet<>(varsX);
                allVars.addAll(varsY);

                try {
                    this.pointXExpr = new ExpressionBuilder(sX).variables(allVars).build();
                    this.pointYExpr = new ExpressionBuilder(sY).variables(allVars).build();

                    // Success
                    this.isPoint = true;

                    // Register params
                    for (String v : allVars) {
                        if (!params.containsKey(v)) params.put(v, 1.0);
                        paramNames.add(v);
                    }
                    return; // Done
                } catch (Exception ignored) {
                    // Failed to parse as expressions, might be regular equation with parens (unlikely but possible)
                    // Fall through to equation parsing
                }
            }
        }

        // 2. Normal Equation Parsing
        String norm = normalize(s);

        // Extract params (excluding x, y)
        Set<String> vars = extractVariables(norm, false);
        for (String p : vars) {
            if (!params.containsKey(p)) params.put(p, 1.0);
            paramNames.add(p);
        }

        // Check for implicit (=)
        if (norm.contains("=")) {
            this.implicit = true;
            String[] parts = norm.split("=", 2);
            String combined = "(" + parts[0] + ")-(" + parts[1] + ")";
            try {
                this.implicitExpr = new ExpressionBuilder(combined)
                        .variables("x", "y")
                        .variables(params.keySet())
                        .build();
            } catch (Exception e) {
                this.implicitExpr = null;
            }
        } else {
            // explicit y = ...
            try {
                this.explicitExpr = new ExpressionBuilder(norm)
                        .variables("x")
                        .variables(params.keySet())
                        .build();
            } catch (Exception e) {
                this.explicitExpr = null;
            }
        }
    }

    // --- Helper: Find comma separating x and y ---
    private int findTopLevelComma(String text) {
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == ',' && depth == 0) return i;
        }
        return -1;
    }

    // --- Helper: Extract variables ---
    private Set<String> extractVariables(String expr, boolean isPointMode) {
        Set<String> vars = new HashSet<>();
        Matcher m = VAR_PATTERN.matcher(expr);
        while (m.find()) {
            String w = m.group();
            if (isPointMode) {
                // In point mode (a,b), everything is a parameter (even x, y, t)
                // except known math functions
                if (!KNOWN_FUNCS.contains(w) || w.equals("x") || w.equals("y")) {
                    // Check if it's strictly a math function name
                    boolean isMath = false;
                    for(String f : KNOWN_FUNCS) {
                        if(f.equals(w) && !f.equals("x") && !f.equals("y")) {
                            isMath = true; break;
                        }
                    }
                    if(!isMath) vars.add(w);
                }
            } else {
                // In equation mode, ignore x, y and functions
                if (!KNOWN_FUNCS.contains(w)) {
                    vars.add(w);
                }
            }
        }
        return vars;
    }

    // --- Helper: Normalize text ---
    private String normalize(String s) {
        return s.toLowerCase().replaceAll("\\s+", "");
    }

    // --- Evaluation ---

    // Get dynamic Point X
    public double getPointX() {
        if (!isPoint || pointXExpr == null) return Double.NaN;
        for (Map.Entry<String, Double> e : params.entrySet()) {
            pointXExpr.setVariable(e.getKey(), e.getValue());
        }
        try { return pointXExpr.evaluate(); } catch (Exception e) { return Double.NaN; }
    }

    // Get dynamic Point Y
    public double getPointY() {
        if (!isPoint || pointYExpr == null) return Double.NaN;
        for (Map.Entry<String, Double> e : params.entrySet()) {
            pointYExpr.setVariable(e.getKey(), e.getValue());
        }
        try { return pointYExpr.evaluate(); } catch (Exception e) { return Double.NaN; }
    }

    public double evalExplicit(double x) {
        if (explicitExpr == null) return Double.NaN;
        explicitExpr.setVariable("x", x);
        for (Map.Entry<String, Double> e : params.entrySet()) {
            explicitExpr.setVariable(e.getKey(), e.getValue());
        }
        try { return explicitExpr.evaluate(); } catch (Exception e) { return Double.NaN; }
    }

    public double evalImplicit(double x, double y) {
        if (implicitExpr == null) return Double.NaN;
        implicitExpr.setVariable("x", x);
        implicitExpr.setVariable("y", y);
        for (Map.Entry<String, Double> e : params.entrySet()) {
            implicitExpr.setVariable(e.getKey(), e.getValue());
        }
        try { return implicitExpr.evaluate(); } catch (Exception e) { return Double.NaN; }
    }

    // Getters & Setters
    public String getRawText() { return rawText; }
    public void setRawText(String t) { this.rawText = t; rebuild(); }

    public Color getColor() { return color; }
    public void setColor(Color c) { this.color = c; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { this.visible = v; }

    public boolean isImplicit() { return implicit; }

    public boolean isPoint() { return isPoint; }

    public boolean isLabelVisible() { return labelVisible; }
    public void setLabelVisible(boolean labelVisible) { this.labelVisible = labelVisible; }

    public List<String> getParamNames() { return paramNames; }
    public void setParam(String name, double val) { params.put(name, val); }
    public double getParam(String name, double def) { return params.getOrDefault(name, def); }
}








