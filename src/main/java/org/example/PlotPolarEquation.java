package com.equationplotter.ui;

import javafx.scene.paint.Color;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlotPolarEquation {

    private String rawText;
    private Color color;
    private boolean visible = true;
    private Expression expr;
    private final Map<String, Double> params = new HashMap<>();
    private final List<String> paramNames = new ArrayList<>();

    private static final Pattern VAR_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    private static final Set<String> KNOWN_FUNCS = new HashSet<>(Arrays.asList(
            "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh",
            "sqrt", "cbrt", "abs", "ceil", "floor", "exp", "log", "log10", "pi", "e", "t"
    ));

    private static final Operator POWER_OPERATOR = new Operator("^", 2, true, Operator.PRECEDENCE_POWER) {
        @Override
        public double apply(double... args) {
            double base = args[0];
            double exponent = args[1];
            if (base < 0) {
                if (Math.abs(exponent - Math.round(exponent)) < 1e-9) return Math.pow(base, exponent);
                for (int q = 3; q <= 11; q += 2) {
                    double p = exponent * q;
                    if (Math.abs(p - Math.round(p)) < 1e-9) {
                        long pInt = Math.round(p);
                        double res = Math.pow(Math.abs(base), exponent);
                        return (pInt % 2 != 0) ? -res : res;
                    }
                }
            }
            return Math.pow(base, exponent);
        }
    };

    public PlotPolarEquation(String text, Color color) {
        this.rawText = text == null ? "" : text;
        this.color = color == null ? Color.BLUE : color;
        rebuild();
    }

    public void rebuild() {
        String s = rawText.trim().toLowerCase().replaceAll("\\s+", "");
        this.expr = null;
        this.paramNames.clear();

        if (s.isEmpty()) return;

        // Strip "r=" if the user types it
        if (s.startsWith("r=")) s = s.substring(2);

        Set<String> vars = new HashSet<>();
        Matcher m = VAR_PATTERN.matcher(s);
        while (m.find()) {
            String w = m.group();
            if (!KNOWN_FUNCS.contains(w)) vars.add(w);
        }

        for (String p : vars) {
            if (!params.containsKey(p)) params.put(p, 1.0);
            paramNames.add(p);
        }

        try {
            this.expr = new ExpressionBuilder(s)
                    .variables("t") // 't' represents theta
                    .variables(params.keySet())
                    .operator(POWER_OPERATOR)
                    .build();
        } catch (Exception e) {
            this.expr = null;
        }
    }

    public double eval(double t) {
        if (expr == null) return Double.NaN;
        expr.setVariable("t", t);
        for (Map.Entry<String, Double> e : params.entrySet()) expr.setVariable(e.getKey(), e.getValue());
        try { return expr.evaluate(); } catch (Exception e) { return Double.NaN; }
    }

    public String getRawText() { return rawText; }
    public void setRawText(String t) { this.rawText = t; rebuild(); }
    public Color getColor() { return color; }
    public void setColor(Color c) { this.color = c; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { this.visible = v; }
    public List<String> getParamNames() { return paramNames; }
    public void setParam(String name, double val) { params.put(name, val); }
    public double getParam(String name, double def) { return params.getOrDefault(name, def); }
}