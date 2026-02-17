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
//package com.equationplotter.ui;
//
//import java.util.*;
//
//public final class ParamExtractor {
//    private ParamExtractor() {}
//
//    // functions to ignore (exp4j/common)
//    private static final Set<String> FUNCS = new HashSet<>(Arrays.asList(
//            "sin","cos","tan","asin","acos","atan",
//            "sqrt","abs","log","ln","floor","ceil","exp",
//            "min","max","pow"
//    ));
//
//    public static List<String> extractParams(String normalizedExpr) {
//        if (normalizedExpr == null) return List.of();
//
//        // collect letter tokens
//        // we will treat SINGLE LETTER params only: a,b,c,m,k...
//        // ignore x,y
//        // ignore known function names
//        Set<String> params = new LinkedHashSet<>();
//
//        String s = normalizedExpr;
//
//        // very simple scan: take letters and group them
//        StringBuilder token = new StringBuilder();
//        for (int i = 0; i < s.length(); i++) {
//            char ch = s.charAt(i);
//            if (Character.isLetter(ch)) {
//                token.append(Character.toLowerCase(ch));
//            } else {
//                flushToken(token, params);
//            }
//        }
//        flushToken(token, params);
//
//        // remove x,y
//        params.remove("x");
//        params.remove("y");
//
//        return new ArrayList<>(params);
//    }
//
//    private static void flushToken(StringBuilder token, Set<String> params) {
//        if (token.length() == 0) return;
//        String t = token.toString();
//        token.setLength(0);
//
//        // function name?
//        if (FUNCS.contains(t)) return;
//
//        // single-letter => parameter
//        if (t.length() == 1) {
//            params.add(t);
//        } else {
//            // multi-letter variable: ignore (optional)
//            // e.g., "theta" হলে এখন skip
//        }
//    }
//}
