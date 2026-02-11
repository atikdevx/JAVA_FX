package com.equationplotter.ui;

import javafx.scene.paint.Color;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class PlotEquation {
    private String rawText;
    private Color color;
    private Expression expr; // compiled expression in terms of x

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

    public double eval(double x) {
        if (expr == null) return Double.NaN;
        try {
            return expr.setVariable("x", x).evaluate();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private void compile() {
        try {
            String cleaned = normalize(rawText);
            if (cleaned.isBlank()) { expr = null; return; }

            expr = new ExpressionBuilder(cleaned)
                    .variable("x")
                    .build();
        } catch (Exception e) {
            expr = null;
        }
    }

    // "y=4x^2" => "4*x^2", "2(x+1)" => "2*(x+1)"
    private String normalize(String s) {
        if (s == null) return "";
        s = s.trim().replace(" ", "");

        if (s.startsWith("y=") || s.startsWith("Y=")) s = s.substring(2);

        // 4x -> 4*x
        s = s.replaceAll("(\\d)(x)", "$1*$2");

        // 2( -> 2*(
        s = s.replaceAll("(\\d)\\(", "$1*(");

        // )x -> )*x
        s = s.replaceAll("\\)(x)", ")*$1");

        return s;
    }
}
