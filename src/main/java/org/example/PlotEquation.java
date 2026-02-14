package com.equationplotter.ui;

import javafx.scene.paint.Color;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

import java.util.*;

public class PlotEquation {

    private String rawText;
    private Color color;
    private boolean visible = true;

    private boolean implicit;

    private Expression explicitExpr;
    private Expression implicitExpr;

    private final Map<String, Double> params = new HashMap<>();
    private final List<String> paramNames = new ArrayList<>();

    // ---------------- Gamma / Factorial ----------------
    // Desmos-like: x! = Gamma(x+1)
    private static double gamma(double z) {
        // Lanczos approximation (good enough for plotter)
        // poles at 0, -1, -2, ...
        if (Double.isNaN(z) || Double.isInfinite(z)) return Double.NaN;

        // reflection for negative
        if (z < 0.5) {
            // Gamma(z) = pi / (sin(pi z) * Gamma(1-z))
            double sin = Math.sin(Math.PI * z);
            if (Math.abs(sin) < 1e-12) return Double.NaN; // near pole
            return Math.PI / (sin * gamma(1.0 - z));
        }

        // Lanczos coefficients (g=7, n=9)
        double[] p = {
                0.99999999999980993,
                676.5203681218851,
                -1259.1392167224028,
                771.32342877765313,
                -176.61502916214059,
                12.507343278686905,
                -0.13857109526572012,
                9.9843695780195716e-6,
                1.5056327351493116e-7
        };

        z -= 1.0;
        double x = p[0];
        for (int i = 1; i < p.length; i++) {
            x += p[i] / (z + i);
        }
        double t = z + 7.5; // g + 0.5 where g=7
        // sqrt(2*pi) * t^(z+0.5) * e^-t * x
        double sqrtTwoPi = 2.5066282746310007;
        double result = sqrtTwoPi * Math.pow(t, z + 0.5) * Math.exp(-t) * x;

        if (Double.isInfinite(result) || Double.isNaN(result)) return Double.NaN;
        return result;
    }

    private static final Function FACT = new Function("fact", 1) {
        @Override
        public double apply(double... args) {
            double x = args[0];
            return gamma(x + 1.0);
        }
    };

    public PlotEquation(String text, Color color) {
        this.rawText = text == null ? "" : text;
        this.color = color == null ? Color.BLUE : color;
        rebuild();
    }

    // ================= NORMALIZE =================
    private String normalize(String s) {
        s = s == null ? "" : s;
        s = s.replaceAll("\\s+", "");

        // --- 1) arc-sin should mean inverse ---
        // arcsin(...) -> asin(...)
        s = s.replaceAll("arcsin\\(", "asin(");
        // arcsinx -> asin(x)
        s = s.replaceAll("arcsin([a-zA-Z0-9])", "asin($1)");

        // --- 2) IMPORTANT: "asinx" should mean a*sin(x) (parameter a) ---
        // but "asin(x)" stays inverse
        s = s.replaceAll("asinx", "a*sin(x)");
        // (optional) if user types asinX (capital) ignore; keeping simple

        // --- 3) sinx / cosx / tanx / absx -> sin(x) style ---
        // (this will NOT affect asin(...) because it's different token)
        s = s.replaceAll("sin([a-zA-Z0-9])", "sin($1)");
        s = s.replaceAll("cos([a-zA-Z0-9])", "cos($1)");
        s = s.replaceAll("tan([a-zA-Z0-9])", "tan($1)");
        s = s.replaceAll("abs([a-zA-Z0-9])", "abs($1)");

        // --- 4) sin^2(x) -> (sin(x))^2 ---
        s = s.replaceAll("sin\\^([0-9]+)\\((.*?)\\)", "(sin($2))^$1");
        s = s.replaceAll("cos\\^([0-9]+)\\((.*?)\\)", "(cos($2))^$1");
        s = s.replaceAll("tan\\^([0-9]+)\\((.*?)\\)", "(tan($2))^$1");

        // --- 5) x! -> fact(x) (Gamma-based) ---
        // also works for (x+1)! etc if user writes (x+1)!
        s = s.replaceAll("(\\([^\\)]+\\)|[a-zA-Z0-9\\.]+)!", "fact($1)");

        return s;
    }

    // ================= BUILD =================
    private void rebuild() {
        paramNames.clear();
        explicitExpr = null;
        implicitExpr = null;

        if (rawText == null || rawText.isBlank()) return;

        String s = normalize(rawText);

        try {
            if (s.contains("=")) {
                implicit = true;

                String[] parts = s.split("=", 2);
                String expr = "(" + parts[0] + ")-(" + parts[1] + ")";

                detectParams(expr, Set.of("x", "y"));

                ExpressionBuilder b = new ExpressionBuilder(expr)
                        .functions(FACT)
                        .variables(merge(Set.of("x", "y"), paramNames));

                implicitExpr = b.build();

            } else {
                implicit = false;

                String expr = s;

                detectParams(expr, Set.of("x"));

                ExpressionBuilder b = new ExpressionBuilder(expr)
                        .functions(FACT)
                        .variables(merge(Set.of("x"), paramNames));

                explicitExpr = b.build();
            }

        } catch (Exception ignored) {
            // keep expr null
        }
    }

    // ================= PARAM DETECT =================
    private void detectParams(String expr, Set<String> ignore) {
        Set<String> tokens = new HashSet<>();

        for (String t : expr.split("[^a-zA-Z]")) {
            if (t == null || t.isBlank()) continue;
            if (ignore.contains(t)) continue;

            // skip known functions
            if (List.of("sin", "cos", "tan", "asin", "acos", "atan", "abs", "fact", "sqrt", "log", "ln", "exp", "pow")
                    .contains(t)) continue;

            tokens.add(t);
        }

        paramNames.addAll(tokens);
        for (String p : tokens) params.putIfAbsent(p, 1.0);
    }

    private Set<String> merge(Set<String> base, List<String> extra) {
        Set<String> s = new HashSet<>(base);
        s.addAll(extra);
        return s;
    }

    // ================= EVAL =================
    public double evalExplicit(double x) {
        if (explicitExpr == null) return Double.NaN;

        try {
            explicitExpr.setVariable("x", x);
            for (String p : paramNames) explicitExpr.setVariable(p, params.getOrDefault(p, 1.0));
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
            for (String p : paramNames) implicitExpr.setVariable(p, params.getOrDefault(p, 1.0));
            return implicitExpr.evaluate();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    // ================= GETTERS =================
    public boolean isImplicit() { return implicit; }
    public boolean isVisible() { return visible; }
    public Color getColor() { return color; }
    public String getRawText() { return rawText; }
    public List<String> getParamNames() { return paramNames; }

    // ================= SETTERS =================
    public void setRawText(String t) {
        rawText = t == null ? "" : t;
        rebuild();
    }

    public void setColor(Color c) { color = c == null ? Color.BLUE : c; }
    public void setVisible(boolean v) { visible = v; }

    public void setParam(String name, double v) { params.put(name, v); }
    public double getParam(String name, double def) { return params.getOrDefault(name, def); }
}
