package com.equationplotter.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EquationNormalizer {
    private EquationNormalizer() {}

    // sin^-1(x) / sin^(-1)(x) -> asin(x)
    private static final Pattern INV_TRIG =
            Pattern.compile("(?i)\\b(sin|cos|tan)\\s*\\^\\s*\\(?\\s*-\\s*1\\s*\\)?\\s*\\(");

    public static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim();

        // remove spaces
        s = s.replaceAll("\\s+", "");

        // dot multiply -> *
        s = s.replace(".", "*");

        // arcsin/arccos/arctan -> asin/acos/atan
        s = s.replaceAll("(?i)arcsin\\(", "asin(");
        s = s.replaceAll("(?i)arccos\\(", "acos(");
        s = s.replaceAll("(?i)arctan\\(", "atan(");

        // sin^-1( -> asin(
        s = replaceInverseTrig(s);

        // insert * for cases like: 2x, 2a, 2(something)
        s = s.replaceAll("(\\d)([a-zA-Z\\(])", "$1*$2");

        // insert * for adjacency like: x y , x a , )x , )a , x( , a(
        // (function names are letters আগে, তাই safe)
        s = s.replaceAll("([xy\\)])([a-zA-Z\\(])", "$1*$2");

        // insert * for adjacency like: a x, a y
        s = s.replaceAll("([a-zA-Z])([xy])", "$1*$2");

        return s;
    }

    private static String replaceInverseTrig(String s) {
        Matcher m = INV_TRIG.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fn = m.group(1).toLowerCase();
            String repl = switch (fn) {
                case "sin" -> "asin(";
                case "cos" -> "acos(";
                case "tan" -> "atan(";
                default -> fn + "(";
            };
            m.appendReplacement(sb, repl);
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
