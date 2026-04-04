package com.equationplotter.ui;

import javafx.scene.paint.Color;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;

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

    // Set of known functions
    private static final Set<String> KNOWN_FUNCS = new HashSet<>(Arrays.asList(
            "sin", "cos", "tan", "sec", "csc", "cosec", "cot",
            "asin", "acos", "atan", "sinh", "cosh", "tanh",
            "sqrt", "cbrt", "abs", "ceil", "floor", "exp", "log", "log10", "log2",
            "signum", "x", "y", "pi", "e"
    ));

    // Custom power operator to handle negative bases with fractional powers (e.g. x^(2/3))
    private static final Operator POWER_OPERATOR = new Operator("^", 2, true, Operator.PRECEDENCE_POWER) {
        @Override
        public double apply(double... args) {
            double base = args[0];
            double exponent = args[1];

            if (base < 0) {
                // If it's an integer exponent, let normal math handle it
                if (Math.abs(exponent - Math.round(exponent)) < 1e-9) {
                    return Math.pow(base, exponent);
                }
                // Check if exponent is a fraction with a small odd denominator (3, 5, 7, 9, 11)
                for (int q = 3; q <= 11; q += 2) {
                    double p = exponent * q;
                    if (Math.abs(p - Math.round(p)) < 1e-9) {
                        // It is a valid fraction!
                        long pInt = Math.round(p);
                        double res = Math.pow(Math.abs(base), exponent);
                        // If the numerator is odd, the result is negative. If even, positive.
                        return (pInt % 2 != 0) ? -res : res;
                    }
                }
            }
            // Default behavior for everything else
            return Math.pow(base, exponent);
        }
    };

    private static final Function LOG10_FUNC = new Function("log10", 1) {
        @Override
        public double apply(double... args) {
            double v = args[0];
            if (v <= 0) return Double.NaN;     // domain error -> break segment
            return Math.log10(v);              // may go very negative near 0+
        }
    };

    private static final Function LN_FUNC = new Function("log", 1) {
        @Override
        public double apply(double... args) {
            double v = args[0];
            if (v <= 0) return Double.NaN;     // domain error -> break segment
            return Math.log(v);                // may go very negative near 0+
        }
    };

    private static double safeReciprocal(double v) {
        return Math.abs(v) < 1e-12 ? Double.NaN : 1.0 / v;
    }

    private static final Function SEC_FUNC = new Function("sec", 1) {
        @Override
        public double apply(double... args) {
            return safeReciprocal(Math.cos(args[0]));
        }
    };

    private static final Function CSC_FUNC = new Function("csc", 1) {
        @Override
        public double apply(double... args) {
            return safeReciprocal(Math.sin(args[0]));
        }
    };

    private static final Function COSEC_FUNC = new Function("cosec", 1) {
        @Override
        public double apply(double... args) {
            return safeReciprocal(Math.sin(args[0]));
        }
    };

    private static final Function COT_FUNC = new Function("cot", 1) {
        @Override
        public double apply(double... args) {
            return safeReciprocal(Math.tan(args[0]));
        }
    };

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

                Set<String> varsX = extractVariables(sX, true);
                Set<String> varsY = extractVariables(sY, true);
                Set<String> allVars = new TreeSet<>(varsX);
                allVars.addAll(varsY);

                try {
                    this.pointXExpr = new ExpressionBuilder(normalize(sX))
                            .variables(allVars)
                            .function(LOG10_FUNC)
                            .function(LN_FUNC)
                            .function(SEC_FUNC)
                            .function(CSC_FUNC)
                            .function(COSEC_FUNC)
                            .function(COT_FUNC)
                            .operator(POWER_OPERATOR)
                            .build();

                    this.pointYExpr = new ExpressionBuilder(normalize(sY))
                            .variables(allVars)
                            .function(LOG10_FUNC)
                            .function(LN_FUNC)
                            .function(SEC_FUNC)
                            .function(CSC_FUNC)
                            .function(COSEC_FUNC)
                            .function(COT_FUNC)
                            .operator(POWER_OPERATOR)
                            .build();
                    this.isPoint = true;
                    for (String v : allVars) {
                        if (!params.containsKey(v)) params.put(v, 1.0);
                        paramNames.add(v);
                    }
                    return;
                } catch (Exception ignored) { }
            }
        }

        // 2. Normal Equation Parsing
        String norm = normalize(s);

        // Extract params
        Set<String> vars = extractVariables(norm, false);
        for (String p : vars) {
            if (!params.containsKey(p)) params.put(p, 1.0);
            paramNames.add(p);
        }

        // SMART EXPLICIT DETECTION
        if (norm.contains("=")) {
            String[] parts = norm.split("=", 2);
            if (parts[0].equals("y") && !parts[1].contains("y")) {
                this.implicit = false;
                norm = parts[1]; // Use RHS
            } else if (parts[1].equals("y") && !parts[0].contains("y")) {
                this.implicit = false;
                norm = parts[0]; // Use LHS
            } else {
                this.implicit = true; // Truly implicit
            }
        } else {
            this.implicit = false;
        }

        // Build Expressions
        if (this.implicit) {
            String[] parts = norm.split("=", 2);
            if (parts.length == 2) {
                String combined = "(" + parts[0] + ")-(" + parts[1] + ")";
                try {
                    this.implicitExpr = new ExpressionBuilder(combined)
                            .variables("x", "y")
                            .variables(params.keySet())
                            .function(LOG10_FUNC)
                            .function(LN_FUNC)
                            .operator(POWER_OPERATOR)
                            .build();
                } catch (Exception e) {
                    this.implicitExpr = null;
                }
            }
        } else {
            try {
                this.explicitExpr = new ExpressionBuilder(norm)
                        .variables("x")
                        .variables(params.keySet())
                        .function(LOG10_FUNC)
                        .function(LN_FUNC)
                        .operator(POWER_OPERATOR)
                        .build();
            } catch (Exception e) {
                this.explicitExpr = null;
            }
        }
    }

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

    private Set<String> extractVariables(String expr, boolean isPointMode) {
        Set<String> vars = new HashSet<>();
        Matcher m = VAR_PATTERN.matcher(expr);
        while (m.find()) {
            String w = m.group();
            if (isPointMode) {
                if (!KNOWN_FUNCS.contains(w) || w.equals("x") || w.equals("y")) {
                    boolean isMath = KNOWN_FUNCS.contains(w) && !w.equals("x") && !w.equals("y");
                    if (!isMath) vars.add(w);
                }
            } else {
                if (!KNOWN_FUNCS.contains(w)) {
                    vars.add(w);
                }
            }
        }
        return vars;
    }

    private String normalize(String s) {
        s = s.toLowerCase().replaceAll("\\s+", "");

        s = s.replace("asin^-1", "F_ASIN");
        s = s.replace("sin^-1", "F_ASIN");
        s = s.replace("acos^-1", "F_ACOS");
        s = s.replace("cos^-1", "F_ACOS");
        s = s.replace("atan^-1", "F_ATAN");
        s = s.replace("tan^-1", "F_ATAN");

        // reciprocal trig aliases first, before sin/cos/tan replacement
        s = s.replace("cosec", "F_COSEC");
        s = s.replace("csc", "F_CSC");
        s = s.replace("sec", "F_SEC");
        s = s.replace("cot", "F_COT");

        s = s.replace("sinh", "F_SINH");
        s = s.replace("cosh", "F_COSH");
        s = s.replace("tanh", "F_TANH");
        s = s.replace("sin", "F_SIN");
        s = s.replace("cos", "F_COS");
        s = s.replace("tan", "F_TAN");
        s = s.replace("sqrt", "F_SQRT");
        s = s.replace("cbrt", "F_CBRT");
        s = s.replace("abs", "F_ABS");
        s = s.replace("exp", "F_EXP");
        s = s.replace("log10", "F_LOGTEN");
        s = s.replace("log", "F_LOGTEN");
        s = s.replace("ln", "F_LN");
        s = s.replace("pi", "F_PI");

        // Protect Step Functions
        s = s.replace("ceil", "F_CEIL");
        s = s.replace("floor", "F_FLOOR");
        s = s.replace("signum", "F_SIGNUM");
        s = s.replace("round", "F_ROUND");

        s = s.replaceAll("(F_[A-Z]+)([a-z0-9]+)", "$1($2)");

        s = s.replaceAll("(\\d)([a-z])", "$1*$2");
        while (s.matches(".*[a-z]{2}.*")) {
            s = s.replaceAll("([a-z])([a-z])", "$1*$2");
        }
        s = s.replaceAll("([a-z])(\\d)", "$1*$2");
        s = s.replaceAll("(\\d)(F_[A-Z]+)", "$1*$2");
        s = s.replaceAll("([a-z])(F_[A-Z]+)", "$1*$2");
        s = s.replaceAll("\\)([a-z0-9]|F_[A-Z]+)", ")*$1");
        s = s.replaceAll("([a-z0-9])\\(", "$1*(");
        s = s.replaceAll("\\)\\(", ")*(");

        s = s.replace("F_ASIN", "asin");
        s = s.replace("F_ACOS", "acos");
        s = s.replace("F_ATAN", "atan");
        s = s.replace("F_SINH", "sinh");
        s = s.replace("F_COSH", "cosh");
        s = s.replace("F_TANH", "tanh");
        s = s.replace("F_SIN", "sin");
        s = s.replace("F_COS", "cos");
        s = s.replace("F_TAN", "tan");
        s = s.replace("F_SEC", "sec");
        s = s.replace("F_CSC", "csc");
        s = s.replace("F_COSEC", "cosec");
        s = s.replace("F_COT", "cot");
        s = s.replace("F_SQRT", "sqrt");
        s = s.replace("F_CBRT", "cbrt");
        s = s.replace("F_ABS", "abs");
        s = s.replace("F_EXP", "exp");
        s = s.replace("F_LOGTEN", "log10");
        s = s.replace("F_LN", "log");
        s = s.replace("F_PI", "pi");

        // Restore Step Functions
        s = s.replace("F_CEIL", "ceil");
        s = s.replace("F_FLOOR", "floor");
        s = s.replace("F_SIGNUM", "signum");
        s = s.replace("F_ROUND", "round");

        return s;
    }

    public double getPointX() {
        if (!isPoint || pointXExpr == null) return Double.NaN;
        for (Map.Entry<String, Double> e : params.entrySet()) pointXExpr.setVariable(e.getKey(), e.getValue());
        try { return pointXExpr.evaluate(); } catch (Exception e) { return Double.NaN; }
    }

    public double getPointY() {
        if (!isPoint || pointYExpr == null) return Double.NaN;
        for (Map.Entry<String, Double> e : params.entrySet()) pointYExpr.setVariable(e.getKey(), e.getValue());
        try { return pointYExpr.evaluate(); } catch (Exception e) { return Double.NaN; }
    }

    public double evalExplicit(double x) {
        if (explicitExpr == null) return Double.NaN;
        explicitExpr.setVariable("x", x);
        for (Map.Entry<String, Double> e : params.entrySet()) explicitExpr.setVariable(e.getKey(), e.getValue());
        try { return explicitExpr.evaluate(); } catch (Exception e) { return Double.NaN; }
    }

    public double evalImplicit(double x, double y) {
        if (implicitExpr == null) return Double.NaN;
        implicitExpr.setVariable("x", x);
        implicitExpr.setVariable("y", y);
        for (Map.Entry<String, Double> e : params.entrySet()) implicitExpr.setVariable(e.getKey(), e.getValue());
        try { return implicitExpr.evaluate(); } catch (Exception e) { return Double.NaN; }
    }

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