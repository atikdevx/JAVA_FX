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
    private static double gamma(double z) {
        if (Double.isNaN(z) || Double.isInfinite(z)) return Double.NaN;

        if (z < 0.5) {
            double sin = Math.sin(Math.PI * z);
            if (Math.abs(sin) < 1e-12) return Double.NaN;
            return Math.PI / (sin * gamma(1.0 - z));
        }

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
        for (int i = 1; i < p.length; i++) x += p[i] / (z + i);

        double t = z + 7.5;
        double sqrtTwoPi = 2.5066282746310007;
        double result = sqrtTwoPi * Math.pow(t, z + 0.5) * Math.exp(-t) * x;

        if (Double.isInfinite(result) || Double.isNaN(result)) return Double.NaN;
        return result;
    }

    private static final Function FACT = new Function("fact", 1) {
        @Override
        public double apply(double... args) {
            return gamma(args[0] + 1.0);
        }
    };

    // 🔥 Desmos-এর মতো পারফেক্ট Asymptote (খাড়া লাইন) লজিক
    private static final Function LN = new Function("ln", 1) {
        @Override
        public double apply(double... args) {
            double x = args[0];
            if (x < -1e-7) return Double.NaN;
            if (Math.abs(x) <= 1e-7) return -50.0;
            return Math.log(x);
        }
    };

    private static final Function LOG = new Function("log", 1) {
        @Override
        public double apply(double... args) {
            double x = args[0];
            if (x < -1e-7) return Double.NaN;
            if (Math.abs(x) <= 1e-7) return -50.0;
            return Math.log10(x);
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
        // y= অংশটুকু শুরুতেই বাদ দেওয়া হলো যাতে y=lnx আর lnx একই আচরণ করে
        s = s.replaceAll("(?i)^y\\s*=\\s*", "");
        s = s.replaceAll("\\s+", "");

        s = s.replaceAll("(?i)sin\\^\\(?\\-1\\)?", "ARCSIN");
        s = s.replaceAll("(?i)cos\\^\\(?\\-1\\)?", "ARCCOS");
        s = s.replaceAll("(?i)tan\\^\\(?\\-1\\)?", "ARCTAN");

        s = s.replaceAll("(?i)arcsin", "ARCSIN");
        s = s.replaceAll("(?i)arccos", "ARCCOS");
        s = s.replaceAll("(?i)arctan", "ARCTAN");

        s = s.replaceAll("(?i)(ARCSIN|ARCCOS|ARCTAN|sin|cos|tan|log|ln|abs|sqrt)([a-zA-Z0-9])", "$1($2)");
        s = s.replaceAll("(?i)([a-zA-Z])(ARCSIN|ARCCOS|ARCTAN|sin|cos|tan|log|ln|abs|sqrt)", "$1*$2");

        s = s.replaceAll("(?i)ARCSIN", "asin");
        s = s.replaceAll("(?i)ARCCOS", "acos");
        s = s.replaceAll("(?i)ARCTAN", "atan");

        s = s.replaceAll("(?i)(sin|cos|tan|asin|acos|atan|log|ln)\\^([0-9]+)\\((.*?)\\)", "($1($3))^$2");
        s = s.replaceAll("(\\([^\\)]+\\)|[a-zA-Z0-9\\.]+)!", "fact($1)");

        s = s.replaceAll("(\\d)([a-zA-Z\\(])", "$1*$2");
        s = s.replaceAll("([xy\\)])([a-zA-Z\\(])", "$1*$2");
        s = s.replaceAll("([a-zA-Z])([xy])", "$1*$2");

        return s;
    }

    // ================= BUILD =================
    private void rebuild() {
        paramNames.clear();
        explicitExpr = null;
        implicitExpr = null;

        if (rawText.isBlank()) return;

        String s = normalize(rawText);

        try {
            if (s.contains("=")) {
                implicit = true;
                String[] parts = s.split("=", 2);
                String expr = "(" + parts[0] + ")-(" + parts[1] + ")";

                detectParams(expr, Set.of("x", "y"));

                implicitExpr = new ExpressionBuilder(expr)
                        .functions(FACT, LN, LOG)
                        .variables(merge(Set.of("x", "y"), paramNames))
                        .build();
            } else {
                implicit = false;
                detectParams(s, Set.of("x"));

                explicitExpr = new ExpressionBuilder(s)
                        .functions(FACT, LN, LOG)
                        .variables(merge(Set.of("x"), paramNames))
                        .build();
            }
        } catch (Exception ignored) {}
    }

    // ================= PARAM DETECT =================
    private void detectParams(String expr, Set<String> ignore) {
        Set<String> found = new LinkedHashSet<>();
        Set<String> funcs = Set.of(
                "sin","cos","tan","asin","acos","atan",
                "abs","sqrt","log","ln","exp","fact","pow"
        );

        StringBuilder token = new StringBuilder();

        for (int i = 0; i < expr.length(); i++) {
            char ch = Character.toLowerCase(expr.charAt(i));
            if (Character.isLetter(ch)) token.append(ch);
            else consumeToken(token, found, ignore, funcs);
        }

        consumeToken(token, found, ignore, funcs);

        paramNames.clear();
        paramNames.addAll(found);

        for (String p : found) params.putIfAbsent(p, 1.0);
    }

    private void consumeToken(StringBuilder token, Set<String> out,
                              Set<String> ignore, Set<String> funcs) {
        if (token.length() == 0) return;
        String t = token.toString();
        token.setLength(0);

        if (ignore.contains(t)) return;
        if (funcs.contains(t)) return;

        // 🔥 e এবং pi কে কনস্ট্যান্ট হিসেবে ধরার ম্যাজিক!
        if (t.equals("e") || t.equals("pi")) return;

        if (t.length() == 1) out.add(t);
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
            for (String p : paramNames)
                explicitExpr.setVariable(p, params.getOrDefault(p, 1.0));

            double result = explicitExpr.evaluate();
            if (Double.isInfinite(result)) {
                return result > 0 ? 10000.0 : -10000.0;
            }
            return result;
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public double evalImplicit(double x, double y) {
        if (implicitExpr == null) return Double.NaN;
        try {
            implicitExpr.setVariable("x", x);
            implicitExpr.setVariable("y", y);
            for (String p : paramNames)
                implicitExpr.setVariable(p, params.getOrDefault(p, 1.0));

            double result = implicitExpr.evaluate();
            if (Double.isInfinite(result)) {
                return result > 0 ? 10000.0 : -10000.0;
            }
            return result;
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    // ================= GETTERS & SETTERS =================
    public boolean isImplicit() { return implicit; }
    public boolean isVisible() { return visible; }
    public Color getColor() { return color; }
    public String getRawText() { return rawText; }
    public List<String> getParamNames() { return paramNames; }

    public void setRawText(String t) {
        rawText = t == null ? "" : t;
        rebuild();
    }

    public void setColor(Color c) { color = c == null ? Color.BLUE : c; }
    public void setVisible(boolean v) { visible = v; }

    public void setParam(String name, double v) { params.put(name, v); }
    public double getParam(String name, double def) { return params.getOrDefault(name, def); }
}
//package com.equationplotter.ui;
//
//import javafx.scene.paint.Color;
//import net.objecthunter.exp4j.Expression;
//import net.objecthunter.exp4j.ExpressionBuilder;
//import net.objecthunter.exp4j.function.Function;
//
//import java.util.*;
//
//public class PlotEquation {
//
//    private String rawText;
//    private Color color;
//    private boolean visible = true;
//
//    private boolean implicit;
//
//    private Expression explicitExpr;
//    private Expression implicitExpr;
//
//    private final Map<String, Double> params = new HashMap<>();
//    private final List<String> paramNames = new ArrayList<>();
//
//    // ---------------- Gamma / Factorial ----------------
//    private static double gamma(double z) {
//        if (Double.isNaN(z) || Double.isInfinite(z)) return Double.NaN;
//
//        if (z < 0.5) {
//            double sin = Math.sin(Math.PI * z);
//            if (Math.abs(sin) < 1e-12) return Double.NaN;
//            return Math.PI / (sin * gamma(1.0 - z));
//        }
//
//        double[] p = {
//                0.99999999999980993,
//                676.5203681218851,
//                -1259.1392167224028,
//                771.32342877765313,
//                -176.61502916214059,
//                12.507343278686905,
//                -0.13857109526572012,
//                9.9843695780195716e-6,
//                1.5056327351493116e-7
//        };
//
//        z -= 1.0;
//        double x = p[0];
//        for (int i = 1; i < p.length; i++) x += p[i] / (z + i);
//
//        double t = z + 7.5;
//        double sqrtTwoPi = 2.5066282746310007;
//        double result = sqrtTwoPi * Math.pow(t, z + 0.5) * Math.exp(-t) * x;
//
//        if (Double.isInfinite(result) || Double.isNaN(result)) return Double.NaN;
//        return result;
//    }
//
//    private static final Function FACT = new Function("fact", 1) {
//        @Override
//        public double apply(double... args) {
//            return gamma(args[0] + 1.0);
//        }
//    };
//
//    // 🔥 লগারিদমের ইনফিনিটি এবং ইমপ্লিসিট ড্রপ ফিক্স
//// 🔥 ইমপ্লিসিট গ্রাফের (y=lnx) গ্রিড অ্যালগরিদমের জন্য স্পেশাল লিনিয়ার এক্সটেনশন
//// 🔥 Discontinuity Detector বাইপাস করার জন্য স্মুথ ট্যানজেন্ট (Tangent) ট্রিক
//    private static final Function LN = new Function("ln", 1) {
//        @Override
//        public double apply(double... args) {
//            double x = args[0];
//            // x এর মান 0.001 এর বড় হলে নরমাল লগারিদম কাজ করবে
//            if (x >= 0.001) {
//                return Math.log(x);
//            }
//            // x এর মান 0 এর কাছাকাছি গেলে গ্রাফটাকে জাম্প না করিয়ে
//            // স্মুথলি খাড়া নিচের দিকে নামিয়ে দেওয়া হলো (Slope = 1000)
//            double y = 1000.0 * (x - 0.001) - 6.90775527898;
//
//            // স্ক্রিনের অনেক নিচে চলে যাওয়ার পর লাইন অফ করে দেবে
//            // যাতে নেগেটিভ দিকে উল্টাপাল্টা দাগ না পড়ে
//            if (y < -200.0) return Double.NaN;
//            return y;
//        }
//    };
//
//    private static final Function LOG = new Function("log", 1) {
//        @Override
//        public double apply(double... args) {
//            double x = args[0];
//            if (x >= 0.001) {
//                return Math.log10(x);
//            }
//            // লগের জন্য স্মুথ ট্যানজেন্ট লাইন
//            double y = 434.2944819 * (x - 0.001) - 3.0;
//
//            if (y < -200.0) return Double.NaN;
//            return y;
//        }
//    };
//
//    public PlotEquation(String text, Color color) {
//        this.rawText = text == null ? "" : text;
//        this.color = color == null ? Color.BLUE : color;
//        rebuild();
//    }
//
//    // ================= NORMALIZE (ULTIMATE FIX) =================
//    private String normalize(String s) {
//        s = s == null ? "" : s;
//        s = s.replaceAll("\\s+", "");
//
//        s = s.replaceAll("(?i)sin\\^\\(?\\-1\\)?", "ARCSIN");
//        s = s.replaceAll("(?i)cos\\^\\(?\\-1\\)?", "ARCCOS");
//        s = s.replaceAll("(?i)tan\\^\\(?\\-1\\)?", "ARCTAN");
//
//        s = s.replaceAll("(?i)arcsin", "ARCSIN");
//        s = s.replaceAll("(?i)arccos", "ARCCOS");
//        s = s.replaceAll("(?i)arctan", "ARCTAN");
//
//        s = s.replaceAll("(?i)(ARCSIN|ARCCOS|ARCTAN|sin|cos|tan|log|ln|abs|sqrt)([a-zA-Z0-9])", "$1($2)");
//        s = s.replaceAll("(?i)([a-zA-Z])(ARCSIN|ARCCOS|ARCTAN|sin|cos|tan|log|ln|abs|sqrt)", "$1*$2");
//
//        s = s.replaceAll("(?i)ARCSIN", "asin");
//        s = s.replaceAll("(?i)ARCCOS", "acos");
//        s = s.replaceAll("(?i)ARCTAN", "atan");
//
//        s = s.replaceAll("(?i)(sin|cos|tan|asin|acos|atan|log|ln)\\^([0-9]+)\\((.*?)\\)", "($1($3))^$2");
//        s = s.replaceAll("(\\([^\\)]+\\)|[a-zA-Z0-9\\.]+)!", "fact($1)");
//
//        s = s.replaceAll("(\\d)([a-zA-Z\\(])", "$1*$2");
//        s = s.replaceAll("([xy\\)])([a-zA-Z\\(])", "$1*$2");
//        s = s.replaceAll("([a-zA-Z])([xy])", "$1*$2");
//
//        return s;
//    }
//
//    // ================= BUILD =================
//    private void rebuild() {
//        paramNames.clear();
//        explicitExpr = null;
//        implicitExpr = null;
//
//        if (rawText.isBlank()) return;
//
//        String s = normalize(rawText);
//
//        try {
//            if (s.contains("=")) {
//                implicit = true;
//                String[] parts = s.split("=", 2);
//                String expr = "(" + parts[0] + ")-(" + parts[1] + ")";
//
//                detectParams(expr, Set.of("x", "y"));
//
//                implicitExpr = new ExpressionBuilder(expr)
//                        .functions(FACT, LN, LOG)
//                        .variables(merge(Set.of("x", "y"), paramNames))
//                        .build();
//            } else {
//                implicit = false;
//                detectParams(s, Set.of("x"));
//
//                explicitExpr = new ExpressionBuilder(s)
//                        .functions(FACT, LN, LOG)
//                        .variables(merge(Set.of("x"), paramNames))
//                        .build();
//            }
//        } catch (Exception ignored) {}
//    }
//
//    // ================= PARAM DETECT =================
//    private void detectParams(String expr, Set<String> ignore) {
//        Set<String> found = new LinkedHashSet<>();
//        Set<String> funcs = Set.of(
//                "sin","cos","tan","asin","acos","atan",
//                "abs","sqrt","log","ln","exp","fact","pow"
//        );
//
//        StringBuilder token = new StringBuilder();
//
//        for (int i = 0; i < expr.length(); i++) {
//            char ch = Character.toLowerCase(expr.charAt(i));
//            if (Character.isLetter(ch)) token.append(ch);
//            else consumeToken(token, found, ignore, funcs);
//        }
//
//        consumeToken(token, found, ignore, funcs);
//
//        paramNames.clear();
//        paramNames.addAll(found);
//
//        for (String p : found) params.putIfAbsent(p, 1.0);
//    }
//
//    private void consumeToken(StringBuilder token, Set<String> out,
//                              Set<String> ignore, Set<String> funcs) {
//        if (token.length() == 0) return;
//        String t = token.toString();
//        token.setLength(0);
//
//        if (ignore.contains(t)) return;
//        if (funcs.contains(t)) return;
//
//        if (t.length() == 1) out.add(t);
//    }
//
//    private Set<String> merge(Set<String> base, List<String> extra) {
//        Set<String> s = new HashSet<>(base);
//        s.addAll(extra);
//        return s;
//    }
//
//    // ================= EVAL =================
//    public double evalExplicit(double x) {
//        if (explicitExpr == null) return Double.NaN;
//        try {
//            explicitExpr.setVariable("x", x);
//            for (String p : paramNames)
//                explicitExpr.setVariable(p, params.getOrDefault(p, 1.0));
//
//            double result = explicitExpr.evaluate();
//            if (Double.isInfinite(result)) {
//                return result > 0 ? 10000.0 : -10000.0;
//            }
//            return result;
//        } catch (Exception e) {
//            return Double.NaN;
//        }
//    }
//
//    public double evalImplicit(double x, double y) {
//        if (implicitExpr == null) return Double.NaN;
//        try {
//            implicitExpr.setVariable("x", x);
//            implicitExpr.setVariable("y", y);
//            for (String p : paramNames)
//                implicitExpr.setVariable(p, params.getOrDefault(p, 1.0));
//
//            double result = implicitExpr.evaluate();
//            if (Double.isInfinite(result)) {
//                return result > 0 ? 10000.0 : -10000.0;
//            }
//            return result;
//        } catch (Exception e) {
//            return Double.NaN;
//        }
//    }
//
//    // ================= GETTERS & SETTERS =================
//    public boolean isImplicit() { return implicit; }
//    public boolean isVisible() { return visible; }
//    public Color getColor() { return color; }
//    public String getRawText() { return rawText; }
//    public List<String> getParamNames() { return paramNames; }
//
//    public void setRawText(String t) {
//        rawText = t == null ? "" : t;
//        rebuild();
//    }
//
//    public void setColor(Color c) { color = c == null ? Color.BLUE : c; }
//    public void setVisible(boolean v) { visible = v; }
//
//    public void setParam(String name, double v) { params.put(name, v); }
//    public double getParam(String name, double def) { return params.getOrDefault(name, def); }
//}