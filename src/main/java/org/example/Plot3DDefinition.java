package com.equationplotter.ui;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Plot3DDefinition {

    public enum Type {
        SURFACE,
        PARAMETRIC_CURVE,
        POINT3D
    }

    private static final Pattern VAR_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    private static final double SCALE = 40.0;

    private String rawText;
    private Color color;
    private boolean visible = true;
    private Type type;

    private Expression surfaceExpr;
    private Expression curveXExpr;
    private Expression curveYExpr;
    private Expression curveZExpr;
    private Expression pointXExpr;
    private Expression pointYExpr;
    private Expression pointZExpr;

    private final Map<String, Double> params = new HashMap<String, Double>();

    public Plot3DDefinition(String rawText, Color color) {
        this.rawText = rawText == null ? "" : rawText.trim();
        this.color = color == null ? Color.CORNFLOWERBLUE : color;
        rebuild();
    }

    public void rebuild() {
        surfaceExpr = null;
        curveXExpr = null;
        curveYExpr = null;
        curveZExpr = null;
        pointXExpr = null;
        pointYExpr = null;
        pointZExpr = null;
        type = null;

        String s = normalize(rawText);
        if (s.isEmpty()) return;

        try {
            tryParsePoint(s);
            if (type != null) return;

            tryParseCurve(s);
            if (type != null) return;

            tryParseSurface(s);
        } catch (Exception ex) {
            type = null;
        }
    }

    private void tryParsePoint(String s) {
        if (!s.startsWith("(") || !s.endsWith(")")) return;

        String content = s.substring(1, s.length() - 1);
        String[] parts = splitTopLevel(content, ',');

        if (parts.length != 3) return;

        Set<String> vars = new HashSet<String>();
        vars.addAll(extractVariables(parts[0]));
        vars.addAll(extractVariables(parts[1]));
        vars.addAll(extractVariables(parts[2]));

        pointXExpr = new ExpressionBuilder(parts[0]).variables(vars).build();
        pointYExpr = new ExpressionBuilder(parts[1]).variables(vars).build();
        pointZExpr = new ExpressionBuilder(parts[2]).variables(vars).build();

        ensureParams(vars);
        type = Type.POINT3D;
    }

    private void tryParseCurve(String s) {
        String[] parts = s.split(";");
        if (parts.length != 3) return;

        String ex = null;
        String ey = null;
        String ez = null;

        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) return;

            String key = kv[0].trim();
            String value = kv[1].trim();

            if (key.equals("x")) ex = value;
            else if (key.equals("y")) ey = value;
            else if (key.equals("z")) ez = value;
        }

        if (ex == null || ey == null || ez == null) return;

        Set<String> vars = new HashSet<String>();
        vars.addAll(extractVariables(ex));
        vars.addAll(extractVariables(ey));
        vars.addAll(extractVariables(ez));
        vars.add("t");

        curveXExpr = new ExpressionBuilder(ex).variables(vars).build();
        curveYExpr = new ExpressionBuilder(ey).variables(vars).build();
        curveZExpr = new ExpressionBuilder(ez).variables(vars).build();

        ensureParams(vars);
        type = Type.PARAMETRIC_CURVE;
    }

    private void tryParseSurface(String s) {
        String expr = s;
        if (s.startsWith("z=")) {
            expr = s.substring(2);
        }

        Set<String> vars = extractVariables(expr);
        vars.add("x");
        vars.add("y");

        surfaceExpr = new ExpressionBuilder(expr).variables(vars).build();
        ensureParams(vars);
        type = Type.SURFACE;
    }

    public Node buildNode() {
        if (type == null) return null;

        if (type == Type.POINT3D) return buildPoint();
        if (type == Type.PARAMETRIC_CURVE) return buildCurve();
        if (type == Type.SURFACE) return buildSurface();

        return null;
    }

    private Node buildPoint() {
        double x = eval(pointXExpr, 0, 0, 0);
        double y = eval(pointYExpr, 0, 0, 0);
        double z = eval(pointZExpr, 0, 0, 0);

        if (bad(x) || bad(y) || bad(z)) return null;

        Sphere point = new Sphere(6);
        point.setMaterial(new PhongMaterial(color));
        point.setTranslateX(x * SCALE);
        point.setTranslateY(-y * SCALE);
        point.setTranslateZ(z * SCALE);

        return point;
    }

    private Node buildCurve() {
        Group group = new Group();

        double tMin = -20;
        double tMax = 20;
        double dt = 0.05;

        double prevX = Double.NaN;
        double prevY = Double.NaN;
        double prevZ = Double.NaN;

        for (double t = tMin; t <= tMax; t += dt) {
            double x = eval(curveXExpr, 0, 0, t);
            double y = eval(curveYExpr, 0, 0, t);
            double z = eval(curveZExpr, 0, 0, t);

            if (bad(x) || bad(y) || bad(z)) {
                prevX = Double.NaN;
                prevY = Double.NaN;
                prevZ = Double.NaN;
                continue;
            }

            if (!Double.isNaN(prevX)) {
                double jump = distance(prevX, prevY, prevZ, x, y, z);
                if (jump < 3.0) {
                    Node seg = createSegment(prevX, prevY, prevZ, x, y, z, color, 1.3);
                    if (seg != null) {
                        group.getChildren().add(seg);
                    }
                }
            }

            prevX = x;
            prevY = y;
            prevZ = z;
        }

        return group;
    }

    private Node buildSurface() {
        int xSteps = 120;
        int ySteps = 120;
        double xMin = -10;
        double xMax = 10;
        double yMin = -10;
        double yMax = 10;

        double dx = (xMax - xMin) / xSteps;
        double dy = (yMax - yMin) / ySteps;

        int[][] index = new int[xSteps + 1][ySteps + 1];
        for (int i = 0; i <= xSteps; i++) {
            Arrays.fill(index[i], -1);
        }

        TriangleMesh mesh = new TriangleMesh();

        for (int ix = 0; ix <= xSteps; ix++) {
            double x = xMin + ix * dx;

            for (int iy = 0; iy <= ySteps; iy++) {
                double y = yMin + iy * dy;
                double z = eval(surfaceExpr, x, y, 0);

                if (bad(z) || Math.abs(z) > 1000) {
                    index[ix][iy] = -1;
                    continue;
                }

                index[ix][iy] = mesh.getPoints().size() / 3;

                mesh.getPoints().addAll(
                        (float) (x * SCALE),
                        (float) (-z * SCALE),
                        (float) (y * SCALE)
                );
            }
        }

        mesh.getTexCoords().addAll(0, 0);

        for (int ix = 0; ix < xSteps; ix++) {
            for (int iy = 0; iy < ySteps; iy++) {
                int p0 = index[ix][iy];
                int p1 = index[ix + 1][iy];
                int p2 = index[ix][iy + 1];
                int p3 = index[ix + 1][iy + 1];

                if (p0 >= 0 && p1 >= 0 && p2 >= 0 && !tooFar(mesh, p0, p1, p2)) {
                    mesh.getFaces().addAll(p0, 0, p1, 0, p2, 0);
                }

                if (p1 >= 0 && p3 >= 0 && p2 >= 0 && !tooFar(mesh, p1, p3, p2)) {
                    mesh.getFaces().addAll(p1, 0, p3, 0, p2, 0);
                }
            }
        }

        if (mesh.getPoints().size() == 0 || mesh.getFaces().size() == 0) {
            return null;
        }

        MeshView fillView = new MeshView(mesh);
        fillView.setDrawMode(DrawMode.FILL);
        fillView.setCullFace(CullFace.NONE);
        fillView.setMaterial(new PhongMaterial(color.deriveColor(0, 1, 1, 0.85)));

        MeshView wireView = new MeshView(mesh);
        wireView.setDrawMode(DrawMode.LINE);
        wireView.setCullFace(CullFace.NONE);
        wireView.setMaterial(new PhongMaterial(Color.color(0, 0, 0, 0.16)));
        wireView.setOpacity(0.18);

        return new Group(fillView, wireView);
    }

    private boolean tooFar(TriangleMesh mesh, int a, int b, int c) {
        Point3D p1 = getPoint(mesh, a);
        Point3D p2 = getPoint(mesh, b);
        Point3D p3 = getPoint(mesh, c);

        double d1 = p1.distance(p2);
        double d2 = p2.distance(p3);
        double d3 = p1.distance(p3);

        return d1 > 120 || d2 > 120 || d3 > 120;
    }

    private Point3D getPoint(TriangleMesh mesh, int index) {
        int base = index * 3;
        float x = mesh.getPoints().get(base);
        float y = mesh.getPoints().get(base + 1);
        float z = mesh.getPoints().get(base + 2);
        return new Point3D(x, y, z);
    }

    private Node createSegment(double x1, double y1, double z1,
                               double x2, double y2, double z2,
                               Color color, double radius) {

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1e-9) return null;

        Cylinder line = new Cylinder(radius, length * SCALE);
        line.setMaterial(new PhongMaterial(color));

        double midX = (x1 + x2) / 2.0;
        double midY = (y1 + y2) / 2.0;
        double midZ = (z1 + z2) / 2.0;

        line.setTranslateX(midX * SCALE);
        line.setTranslateY(-midY * SCALE);
        line.setTranslateZ(midZ * SCALE);

        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = new Point3D(dx, -dy, dz);

        if (diff.magnitude() > 1e-6) {
            Point3D axis = yAxis.crossProduct(diff);
            double dot = yAxis.normalize().dotProduct(diff.normalize());
            dot = Math.max(-1.0, Math.min(1.0, dot));
            double angle = Math.toDegrees(Math.acos(dot));

            if (axis.magnitude() > 1e-6 && !Double.isNaN(angle)) {
                line.getTransforms().add(new Rotate(-angle, axis));
            }
        }

        return line;
    }

    private double distance(double x1, double y1, double z1,
                            double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void ensureParams(Set<String> vars) {
        for (String v : vars) {
            if (v.equals("x") || v.equals("y") || v.equals("z") || v.equals("t")
                    || v.equals("pi") || v.equals("e")) {
                continue;
            }
            if (!params.containsKey(v)) {
                params.put(v, 1.0);
            }
        }
    }

    private Set<String> extractVariables(String expr) {
        Set<String> vars = new HashSet<String>();
        Matcher m = VAR_PATTERN.matcher(expr);

        while (m.find()) {
            String token = m.group();
            if (!isReserved(token)) {
                vars.add(token);
            }
        }

        return vars;
    }

    private boolean isReserved(String token) {
        return token.equals("sin")
                || token.equals("cos")
                || token.equals("tan")
                || token.equals("asin")
                || token.equals("acos")
                || token.equals("atan")
                || token.equals("sqrt")
                || token.equals("abs")
                || token.equals("log")
                || token.equals("log10")
                || token.equals("exp")
                || token.equals("floor")
                || token.equals("ceil")
                || token.equals("pow")
                || token.equals("min")
                || token.equals("max")
                || token.equals("pi")
                || token.equals("e");
    }

    private String normalize(String s) {
        return s.toLowerCase().replaceAll("\\s+", "");
    }

    private String[] splitTopLevel(String text, char delimiter) {
        List<String> result = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '(') depth++;
            if (c == ')') depth--;

            if (c == delimiter && depth == 0) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private double eval(Expression expr, double x, double y, double t) {
        if (expr == null) return Double.NaN;

        try {
            expr.setVariable("x", x);
            expr.setVariable("y", y);
            expr.setVariable("t", t);

            for (Map.Entry<String, Double> entry : params.entrySet()) {
                expr.setVariable(entry.getKey(), entry.getValue());
            }

            return expr.evaluate();
        } catch (Exception ex) {
            return Double.NaN;
        }
    }

    private boolean bad(double v) {
        return Double.isNaN(v) || Double.isInfinite(v);
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText == null ? "" : rawText.trim();
        rebuild();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color != null) {
            this.color = color;
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Type getType() {
        return type;
    }
}