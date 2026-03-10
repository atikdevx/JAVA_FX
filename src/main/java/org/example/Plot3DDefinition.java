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
        POINT3D,
        IMPLICIT
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

    private final Map<String, Double> params = new HashMap<>();

    public Plot3DDefinition(String rawText, Color color) {
        this.rawText = rawText == null ? "" : rawText.trim();
        this.color = color == null ? Color.HOTPINK : color;
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

            tryParseImplicit(s);
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

        Set<String> vars = new HashSet<>();
        vars.addAll(extractVariables(parts[0]));
        vars.addAll(extractVariables(parts[1]));
        vars.addAll(extractVariables(parts[2]));

        vars.addAll(Arrays.asList("x", "y", "z", "t"));
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

        Set<String> vars = new HashSet<>();
        vars.addAll(extractVariables(ex));
        vars.addAll(extractVariables(ey));
        vars.addAll(extractVariables(ez));

        vars.addAll(Arrays.asList("x", "y", "z", "t"));
        curveXExpr = new ExpressionBuilder(ex).variables(vars).build();
        curveYExpr = new ExpressionBuilder(ey).variables(vars).build();
        curveZExpr = new ExpressionBuilder(ez).variables(vars).build();

        ensureParams(vars);
        type = Type.PARAMETRIC_CURVE;
    }

    private void tryParseImplicit(String s) {
        String exprStr = null;
        if (s.contains("=")) {
            if (s.startsWith("z=")) {
                String rhs = s.substring(2);
                if (extractVariables(rhs).contains("z")) {
                    exprStr = "z-(" + rhs + ")";
                } else {
                    return;
                }
            } else {
                String[] parts = s.split("=");
                if (parts.length == 2) {
                    exprStr = "(" + parts[0] + ")-(" + parts[1] + ")";
                } else return;
            }
        } else {
            Set<String> v = extractVariables(s);
            if (v.contains("z")) {
                exprStr = "(" + s + ")-25"; // Default radius 5 for x^2+y^2+z^2
            } else {
                return;
            }
        }

        try {
            Set<String> vars = extractVariables(exprStr);
            vars.addAll(Arrays.asList("x", "y", "z", "t"));
            surfaceExpr = new ExpressionBuilder(exprStr).variables(vars).build();
            ensureParams(vars);
            type = Type.IMPLICIT;
        } catch (Exception e) {}
    }

    private void tryParseSurface(String s) {
        if (s.contains("=") && !s.startsWith("z=")) {
            type = null;
            return;
        }

        String expr = s;
        if (s.startsWith("z=")) {
            expr = s.substring(2);
        }

        Set<String> vars = extractVariables(expr);
        vars.addAll(Arrays.asList("x", "y", "z", "t"));

        surfaceExpr = new ExpressionBuilder(expr).variables(vars).build();
        ensureParams(vars);
        type = Type.SURFACE;
    }

    public Node buildNode() {
        if (type == null) return null;

        if (type == Type.POINT3D) return buildPoint();
        if (type == Type.PARAMETRIC_CURVE) return buildCurve();
        if (type == Type.SURFACE) return buildSurface();
        if (type == Type.IMPLICIT) return buildImplicitSurface();

        return null;
    }

    private Node buildPoint() {
        double x = eval(pointXExpr, 0, 0, 0, 0);
        double y = eval(pointYExpr, 0, 0, 0, 0);
        double z = eval(pointZExpr, 0, 0, 0, 0);

        if (bad(x) || bad(y) || bad(z)) return null;

        Sphere point = new Sphere(6.5);
        point.setMaterial(createMaterial(color));
        point.setTranslateX(x * SCALE);
        point.setTranslateY(-y * SCALE);
        point.setTranslateZ(z * SCALE);
        return point;
    }

    private Node buildCurve() {
        Group group = new Group();

        double prevX = Double.NaN, prevY = Double.NaN, prevZ = Double.NaN;
        double tMin = -16, tMax = 16, dt = 0.04;

        for (double t = tMin; t <= tMax; t += dt) {
            double x = eval(curveXExpr, 0, 0, 0, t);
            double y = eval(curveYExpr, 0, 0, 0, t);
            double z = eval(curveZExpr, 0, 0, 0, t);

            if (bad(x) || bad(y) || bad(z)) {
                prevX = Double.NaN; prevY = Double.NaN; prevZ = Double.NaN;
                continue;
            }

            if (!Double.isNaN(prevX)) {
                double jump = distance(prevX, prevY, prevZ, x, y, z);
                if (jump < 2.0) {
                    Node seg = createSegment(prevX, prevY, prevZ, x, y, z, color, 1.35);
                    if (seg != null) group.getChildren().add(seg);
                }
            }
            prevX = x; prevY = y; prevZ = z;
        }
        return group;
    }

    private Node buildSurface() {
        int xSteps = 180, ySteps = 180;
        double xMin = -6.0, xMax = 6.0, yMin = -6.0, yMax = 6.0;
        double dx = (xMax - xMin) / xSteps;
        double dy = (yMax - yMin) / ySteps;
        double Z_LIMIT = 10.25;

        int[][] index = new int[xSteps + 1][ySteps + 1];
        for (int i = 0; i <= xSteps; i++) Arrays.fill(index[i], -1);

        TriangleMesh mesh = new TriangleMesh();

        for (int ix = 0; ix <= xSteps; ix++) {
            double x = xMin + ix * dx;
            for (int iy = 0; iy <= ySteps; iy++) {
                double y = yMin + iy * dy;
                double z = eval(surfaceExpr, x, y, 0, 0);

                if (bad(z)) {
                    index[ix][iy] = -1;
                    continue;
                }

                // Smooth Clipping Clamping
                if (z > Z_LIMIT) z = Z_LIMIT;
                if (z < -Z_LIMIT) z = -Z_LIMIT;

                index[ix][iy] = mesh.getPoints().size() / 3;
                mesh.getPoints().addAll((float) (x * SCALE), (float) (-z * SCALE), (float) (y * SCALE));
            }
        }

        mesh.getTexCoords().addAll(0, 0);

        for (int ix = 0; ix < xSteps; ix++) {
            for (int iy = 0; iy < ySteps; iy++) {
                int p0 = index[ix][iy], p1 = index[ix + 1][iy];
                int p2 = index[ix][iy + 1], p3 = index[ix + 1][iy + 1];

                if (p0 >= 0 && p1 >= 0 && p2 >= 0 && !tooFar(mesh, p0, p1, p2)) {
                    if (!isAllClamped(mesh, p0, p1, p2)) mesh.getFaces().addAll(p0, 0, p1, 0, p2, 0);
                }
                if (p1 >= 0 && p3 >= 0 && p2 >= 0 && !tooFar(mesh, p1, p3, p2)) {
                    if (!isAllClamped(mesh, p1, p3, p2)) mesh.getFaces().addAll(p1, 0, p3, 0, p2, 0);
                }
            }
        }

        if (mesh.getPoints().size() == 0 || mesh.getFaces().size() == 0) return null;

        MeshView fillView = new MeshView(mesh);
        fillView.setDrawMode(DrawMode.FILL);
        fillView.setCullFace(CullFace.NONE);
        fillView.setMaterial(createMaterial(color));

        return new Group(fillView);
    }

    private Node buildImplicitSurface() {
        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0, 0);

        double min = -12.0, max = 12.0, step = 0.6;
        int steps = (int)Math.ceil((max - min) / step);

        double[][][] val = new double[steps+1][steps+1][steps+1];
        for (int i=0; i<=steps; i++) {
            for (int j=0; j<=steps; j++) {
                for (int k=0; k<=steps; k++) {
                    val[i][j][k] = eval(surfaceExpr, min + i*step, min + j*step, min + k*step, 0);
                }
            }
        }

        int[][] tets = { {0,1,2,6}, {0,2,3,6}, {0,3,7,6}, {0,7,4,6}, {0,4,5,6}, {0,5,1,6} };
        int[][] corners = { {0,0,0}, {1,0,0}, {1,1,0}, {0,1,0}, {0,0,1}, {1,0,1}, {1,1,1}, {0,1,1} };

        for (int i=0; i<steps; i++) {
            for (int j=0; j<steps; j++) {
                for (int k=0; k<steps; k++) {
                    double[] v = new double[8];
                    Point3D[] p = new Point3D[8];
                    boolean hasNaN = false;
                    for(int c=0; c<8; c++) {
                        int ci = i + corners[c][0], cj = j + corners[c][1], ck = k + corners[c][2];
                        v[c] = val[ci][cj][ck];
                        if(Double.isNaN(v[c])) hasNaN = true;
                        p[c] = new Point3D(min + ci*step, min + cj*step, min + ck*step);
                    }
                    if(hasNaN) continue;

                    for(int[] tet : tets) {
                        processTetrahedron(mesh,
                                p[tet[0]], v[tet[0]], p[tet[1]], v[tet[1]],
                                p[tet[2]], v[tet[2]], p[tet[3]], v[tet[3]]
                        );
                    }
                }
            }
        }

        if (mesh.getPoints().size() == 0) return null;

        MeshView view = new MeshView(mesh);
        view.setDrawMode(DrawMode.FILL);
        view.setCullFace(CullFace.NONE);
        view.setMaterial(createMaterial(color));
        return new Group(view);
    }

    private void processTetrahedron(TriangleMesh mesh, Point3D p0, double v0, Point3D p1, double v1, Point3D p2, double v2, Point3D p3, double v3) {
        Point3D[] p = {p0, p1, p2, p3};
        double[] v = {v0, v1, v2, v3};

        List<Integer> pos = new ArrayList<>(), neg = new ArrayList<>();
        for(int i=0; i<4; i++) {
            if(v[i] >= 0) pos.add(i);
            else neg.add(i);
        }

        if(pos.size() == 0 || pos.size() == 4) return;

        if(pos.size() == 1 || pos.size() == 3) {
            int isolated = pos.size() == 1 ? pos.get(0) : neg.get(0);
            List<Integer> others = pos.size() == 1 ? neg : pos;
            Point3D i1 = interpolate(p[isolated], v[isolated], p[others.get(0)], v[others.get(0)]);
            Point3D i2 = interpolate(p[isolated], v[isolated], p[others.get(1)], v[others.get(1)]);
            Point3D i3 = interpolate(p[isolated], v[isolated], p[others.get(2)], v[others.get(2)]);
            addTriangle(mesh, i1, i2, i3);
        } else if(pos.size() == 2) {
            int pA = pos.get(0), pB = pos.get(1);
            int nA = neg.get(0), nB = neg.get(1);
            Point3D i1 = interpolate(p[pA], v[pA], p[nA], v[nA]);
            Point3D i2 = interpolate(p[pA], v[pA], p[nB], v[nB]);
            Point3D i3 = interpolate(p[pB], v[pB], p[nA], v[nA]);
            Point3D i4 = interpolate(p[pB], v[pB], p[nB], v[nB]);
            addTriangle(mesh, i1, i3, i4);
            addTriangle(mesh, i1, i4, i2);
        }
    }

    private Point3D interpolate(Point3D p1, double v1, Point3D p2, double v2) {
        if (Math.abs(v1 - v2) < 1e-5) return p1;
        double t = -v1 / (v2 - v1);
        return p1.add(p2.subtract(p1).multiply(t));
    }

    private void addTriangle(TriangleMesh mesh, Point3D a, Point3D b, Point3D c) {
        int base = mesh.getPoints().size() / 3;
        mesh.getPoints().addAll((float)(a.getX() * SCALE), (float)(-a.getZ() * SCALE), (float)(a.getY() * SCALE));
        mesh.getPoints().addAll((float)(b.getX() * SCALE), (float)(-b.getZ() * SCALE), (float)(b.getY() * SCALE));
        mesh.getPoints().addAll((float)(c.getX() * SCALE), (float)(-c.getZ() * SCALE), (float)(c.getY() * SCALE));
        mesh.getFaces().addAll(base, 0, base+1, 0, base+2, 0);
    }

    private PhongMaterial createMaterial(Color base) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(base);
        // Reduced specular intensity to fix the white spot issue
        material.setSpecularColor(Color.color(1, 1, 1, 0.15));
        return material;
    }

    private boolean isAllClamped(TriangleMesh mesh, int pA, int pB, int pC) {
        double limit = 10.25 * SCALE * 0.999;
        boolean top = getPoint(mesh, pA).getY() <= -limit && getPoint(mesh, pB).getY() <= -limit && getPoint(mesh, pC).getY() <= -limit;
        boolean bottom = getPoint(mesh, pA).getY() >= limit && getPoint(mesh, pB).getY() >= limit && getPoint(mesh, pC).getY() >= limit;
        return top || bottom;
    }

    private boolean tooFar(TriangleMesh mesh, int a, int b, int c) {
        Point3D p1 = getPoint(mesh, a), p2 = getPoint(mesh, b), p3 = getPoint(mesh, c);
        // Increased tolerance to prevent mesh tearing on slopes
        return p1.distance(p2) > 60 || p2.distance(p3) > 60 || p1.distance(p3) > 60;
    }

    private Point3D getPoint(TriangleMesh mesh, int index) {
        int base = index * 3;
        return new Point3D(mesh.getPoints().get(base), mesh.getPoints().get(base + 1), mesh.getPoints().get(base + 2));
    }

    private Node createSegment(double x1, double y1, double z1, double x2, double y2, double z2, Color color, double radius) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1e-9) return null;

        Cylinder line = new Cylinder(radius, length * SCALE);
        line.setMaterial(createMaterial(color));
        line.setCullFace(CullFace.NONE);

        line.setTranslateX(((x1 + x2) / 2.0) * SCALE);
        line.setTranslateY(-((y1 + y2) / 2.0) * SCALE);
        line.setTranslateZ(((z1 + z2) / 2.0) * SCALE);

        Point3D diff = new Point3D(dx, -dy, dz);
        if (diff.magnitude() > 1e-6) {
            Point3D axis = new Point3D(0, 1, 0).crossProduct(diff);
            double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, new Point3D(0, 1, 0).normalize().dotProduct(diff.normalize())))));
            if (axis.magnitude() > 1e-6 && !Double.isNaN(angle)) line.getTransforms().add(new Rotate(-angle, axis));
        }
        return line;
    }

    private double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    private void ensureParams(Set<String> vars) {
        for (String v : vars) {
            if (v.equals("x") || v.equals("y") || v.equals("z") || v.equals("t") || v.equals("pi") || v.equals("e")) continue;
            if (!params.containsKey(v)) params.put(v, 1.0);
        }
    }

    private Set<String> extractVariables(String expr) {
        Set<String> vars = new HashSet<>();
        Matcher m = VAR_PATTERN.matcher(expr);
        while (m.find()) {
            String token = m.group();
            if (!isReserved(token)) vars.add(token);
        }
        return vars;
    }

    private boolean isReserved(String token) {
        return Arrays.asList("sin","cos","tan","asin","acos","atan","sqrt","abs","log","log10","exp","floor","ceil","pow","min","max","pi","e").contains(token);
    }

    private String normalize(String s) {
        return s.toLowerCase().replaceAll("\\s+", "");
    }

    private String[] splitTopLevel(String text, char delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') depth++;
            if (c == ')') depth--;
            if (c == delimiter && depth == 0) { result.add(current.toString()); current.setLength(0); }
            else { current.append(c); }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private double eval(Expression expr, double x, double y, double z, double t) {
        if (expr == null) return Double.NaN;
        try {
            expr.setVariable("x", x);
            expr.setVariable("y", y);
            expr.setVariable("z", z);
            expr.setVariable("t", t);
            for (Map.Entry<String, Double> entry : params.entrySet()) {
                if (!Arrays.asList("x","y","z","t").contains(entry.getKey())) expr.setVariable(entry.getKey(), entry.getValue());
            }
            return expr.evaluate();
        } catch (Exception ex) {
            return Double.NaN;
        }
    }

    private boolean bad(double v) {
        return Double.isNaN(v) || Double.isInfinite(v);
    }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText == null ? "" : rawText.trim(); rebuild(); }
    public Color getColor() { return color; }
    public void setColor(Color color) { if (color != null) this.color = color; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public Type getType() { return type; }
}