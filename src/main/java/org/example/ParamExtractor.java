package com.equationplotter.ui;

import java.util.*;

public final class ParamExtractor {
    private ParamExtractor() {}

    // functions to ignore (exp4j/common)
    private static final Set<String> FUNCS = new HashSet<>(Arrays.asList(
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sqrt", "abs", "log", "log10", "ln", "floor", "ceil", "exp",
            "min", "max", "pow", "pi", "e"
    ));

    public static List<String> extractParams(String normalizedExpr) {
        if (normalizedExpr == null) return List.of();

        Set<String> params = new LinkedHashSet<>();
        String s = normalizedExpr;

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isLetter(ch)) {
                token.append(Character.toLowerCase(ch));
            } else {
                flushToken(token, params);
            }
        }
        flushToken(token, params);

        params.remove("x");
        params.remove("y");

        return new ArrayList<>(params);
    }

    private static void flushToken(StringBuilder token, Set<String> params) {
        if (token.length() == 0) return;
        String t = token.toString();
        token.setLength(0);

        if (FUNCS.contains(t)) return;

        // single-letter parameter extraction
        if (t.length() == 1) {
            params.add(t);
        }
    }
}