package com.equationplotter.ui;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Plot3DDefinition {

    public enum Type {
        SURFACE,
        PARAMETRIC_CURVE,
        POINT3D,
        IMPLICIT,
        EXTRUDED_Y_BY_X,
        EXTRUDED_X_BY_Y
    }

    private final Timer debounceTimer = new Timer(true);
    private volatile Thread buildThread;
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
    private Expression extrudedExpr;

    private final Map<String, Double> params = new HashMap<>();

    public Plot3DDefinition(String rawText, Color color) {
        this.rawText = rawText == null ? "" : rawText.trim();
        this.color = color == null ? Color.HOTPINK : color;
        rebuild();
    }

    public void rebuildAsync(Runnable callback) {
        if (buildThread != null && buildThread.isAlive()) {
            buildThread.interrupt();
        }

        debounceTimer.purge();

        debounceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                buildThread = new Thread(() -> {
                    rebuild();

                    if (Thread.currentThread().isInterrupted()) return;

                    Node node = buildNode();

                    if (node != null && callback != null && !Thread.currentThread().isInterrupted()) {
                        javafx.application.Platform.runLater(callback);
                    }
                });
                buildThread.setDaemon(true);
                buildThread.start();
            }
        }, 150); // Dropped debounce slightly for more responsive slider feeling
    }

    public void rebuild() {
        surfaceExpr = null;
        curveXExpr = null;
        curveYExpr = null;
        curveZExpr = null;
        pointXExpr = null;
        pointYExpr = null;
        pointZExpr = null;
        extrudedExpr = null;
        type = null;

        String s = normalize(rawText);
        if (s.isEmpty()) {
            params.clear();
            return;
        }

        try {
            tryParsePoint(s);
            if (type != null) return;

            tryParseCurve(s);
            if (type != null) return;

            tryParseExtrudedRelation(s);
            if (type != null) return;

            tryParseImplicit(s);
            if (type != null) return;

            tryParseSurface(s);
        } catch (Exception ex) {
            type = null;
        }
    }

    // --- PARSING LOGIC ---

    private void tryParsePoint(String s) {
        if (!s.startsWith("(") || !s.endsWith(")")) return;
        String content = s.substring(1, s.length() - 1);
        String[] parts = splitTopLevel(content, ',');
        if (parts.length != 3) return;

        Set<String> vars = new HashSet<>();
        for (String p : parts) vars.addAll(extractVariables(p));
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

        String ex = null, ey = null, ez = null;
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

    private void tryParseExtrudedRelation(String s) {
        if (!s.contains("=")) return;
        String[] parts = s.split("=", 2);
        if (parts.length != 2) return;

        String left = parts[0].trim();
        String right = parts[1].trim();

        Set<String> vars = new HashSet<>();
        vars.addAll(extractVariables(left));
        vars.addAll(extractVariables(right));
        vars.remove("z");

        for (String v : extractVariables(left + right)) {
            if (!v.equals("x") && !v.equals("y")) return;
        }

        try {
            Set<String> exprVars = new HashSet<>(Arrays.asList("x", "y", "z", "t"));
            if (left.equals("y")) {
                extrudedExpr = new ExpressionBuilder(right).variables(exprVars).build();
                type = Type.EXTRUDED_Y_BY_X;
            } else if (right.equals("y")) {
                extrudedExpr = new ExpressionBuilder(left).variables(exprVars).build();
                type = Type.EXTRUDED_Y_BY_X;
            } else if (left.equals("x")) {
                extrudedExpr = new ExpressionBuilder(right).variables(exprVars).build();
                type = Type.EXTRUDED_X_BY_Y;
            } else if (right.equals("x")) {
                extrudedExpr = new ExpressionBuilder(left).variables(exprVars).build();
                type = Type.EXTRUDED_X_BY_Y;
            }
            ensureParams(vars);
        } catch (Exception ignored) {
            type = null;
        }
    }

    private void tryParseImplicit(String s) {
        int eq = s.indexOf('=');
        if (eq <= 0 || eq != s.lastIndexOf('=')) return;

        String left = s.substring(0, eq).trim();
        String right = s.substring(eq + 1).trim();

        if (left.isEmpty() || right.isEmpty()) return;

        String exprStr = "(" + left + ")-(" + right + ")";

        try {
            Set<String> vars = extractVariables(exprStr);
            vars.addAll(Arrays.asList("x", "y", "z", "t"));
            surfaceExpr = new ExpressionBuilder(exprStr).variables(vars).build();
            ensureParams(vars);
            type = Type.IMPLICIT;
        } catch (Exception e) {
            type = null;
        }
    }

    private void tryParseSurface(String s) {
        String expr;

        if (s.contains("=")) {
            if (!s.startsWith("z=")) return;
            expr = s.substring(2).trim();
            if (expr.isEmpty()) return;

            if (extractVariables(expr).contains("z")) return;
        } else {
            if (extractVariables(s).contains("z")) return;
            expr = s;
        }

        Set<String> vars = extractVariables(expr);
        vars.addAll(Arrays.asList("x", "y", "z", "t"));
        surfaceExpr = new ExpressionBuilder(expr).variables(vars).build();
        ensureParams(vars);
        type = Type.SURFACE;
    }

    // --- BUILDING LOGIC ---

    public Node buildNode() {
        if (type == null) return null;
        return switch (type) {
            case POINT3D -> buildPoint();
            case PARAMETRIC_CURVE -> buildCurve();
            case SURFACE -> buildSurface();
            case IMPLICIT -> {
                Node sphere = buildAnalyticSphereIfPossible();
                if (sphere != null) yield sphere;
                yield buildImplicitSurface();
            }
            case EXTRUDED_Y_BY_X, EXTRUDED_X_BY_Y -> buildExtrudedRelation();
        };
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
            if (Thread.currentThread().isInterrupted()) return null; // STOP IF CANCELLED

            double x = eval(curveXExpr, 0, 0, 0, t);
            double y = eval(curveYExpr, 0, 0, 0, t);
            double z = eval(curveZExpr, 0, 0, 0, t);

            if (bad(x) || bad(y) || bad(z)) {
                prevX = Double.NaN;
                continue;
            }

            if (!Double.isNaN(prevX)) {
                double jump = distance(prevX, prevY, prevZ, x, y, z);
                if (jump < 2.0) {
                    Node seg = createSegment(prevX, prevY, prevZ, x, y, z, color, 1.35);
                    if (seg != null) group.getChildren().add(seg);
                }
            }
            prevX = x;
            prevY = y;
            prevZ = z;
        }
        return group;
    }

    private Node buildSurface() {
        int xSteps = 300, ySteps = 300;
        double xMin = -6.0, xMax = 6.0, yMin = -6.0, yMax = 6.0;

        double dx = (xMax - xMin) / xSteps;
        double dy = (yMax - yMin) / ySteps;

        double Z_LIMIT = 10.25;

        int[][] index = new int[xSteps + 1][ySteps + 1];
        TriangleMesh mesh = new TriangleMesh();

        for (int ix = 0; ix <= xSteps; ix++) {
            if (Thread.currentThread().isInterrupted()) return null; // STOP IF CANCELLED

            double x = xMin + ix * dx;
            for (int iy = 0; iy <= ySteps; iy++) {
                double y = yMin + iy * dy;
                double z = eval(surfaceExpr, x, y, 0, 0);

                if (bad(z)) {
                    index[ix][iy] = -1;
                    continue;
                }

                z = Math.max(-Z_LIMIT, Math.min(Z_LIMIT, z));
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
                    if (!isAllClamped(mesh, p0, p1, p2))
                        mesh.getFaces().addAll(p0, 0, p1, 0, p2, 0);
                }

                if (p1 >= 0 && p3 >= 0 && p2 >= 0 && !tooFar(mesh, p1, p3, p2)) {
                    if (!isAllClamped(mesh, p1, p3, p2))
                        mesh.getFaces().addAll(p1, 0, p3, 0, p2, 0);
                }
            }
        }

        MeshView fillView = new MeshView(mesh);

        int faceCount = mesh.getFaces().size() / 6;
        if (faceCount > 0) {
            int[] smoothingGroups = new int[faceCount];
            Arrays.fill(smoothingGroups, 1);
            mesh.getFaceSmoothingGroups().addAll(smoothingGroups);
        }

        fillView.setCullFace(CullFace.NONE);
        fillView.setMaterial(createMaterial(color));

        return new Group(fillView);
    }

    private Node buildExtrudedRelation() {
        int axisSteps = 220;
        int depthSteps = 80;

        double axisMin = -6.0, axisMax = 6.0;
        double depthMin = -10.0, depthMax = 10.0;

        double axisStep = (axisMax - axisMin) / axisSteps;
        double depthStep = (depthMax - depthMin) / depthSteps;

        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0, 0);

        int[][] idx = new int[axisSteps + 1][depthSteps + 1];

        for (int i = 0; i <= axisSteps; i++) {
            if (Thread.currentThread().isInterrupted()) return null; // STOP IF CANCELLED

            double a = axisMin + i * axisStep;

            double value = (type == Type.EXTRUDED_Y_BY_X)
                    ? eval(extrudedExpr, a, 0, 0, 0)
                    : eval(extrudedExpr, 0, a, 0, 0);

            if (bad(value)) {
                Arrays.fill(idx[i], -1);
                continue;
            }

            for (int j = 0; j <= depthSteps; j++) {
                double depth = depthMin + j * depthStep;

                idx[i][j] = mesh.getPoints().size() / 3;

                float px, py, pz;
                if (type == Type.EXTRUDED_Y_BY_X) {
                    px = (float) (a * SCALE);
                    py = (float) (-value * SCALE);
                    pz = (float) (depth * SCALE);
                } else {
                    px = (float) (value * SCALE);
                    py = (float) (-a * SCALE);
                    pz = (float) (depth * SCALE);
                }

                mesh.getPoints().addAll(px, py, pz);
            }
        }

        List<Integer> smoothingGroups = new ArrayList<>();

        for (int i = 0; i < axisSteps; i++) {
            for (int j = 0; j < depthSteps; j++) {
                int p0 = idx[i][j];
                int p1 = idx[i + 1][j];
                int p2 = idx[i][j + 1];
                int p3 = idx[i + 1][j + 1];

                if (p0 < 0 || p1 < 0 || p2 < 0 || p3 < 0) continue;

                mesh.getFaces().addAll(p0, 0, p1, 0, p2, 0);
                smoothingGroups.add(1);

                mesh.getFaces().addAll(p1, 0, p3, 0, p2, 0);
                smoothingGroups.add(1);
            }
        }

        if (mesh.getPoints().size() == 0 || mesh.getFaces().size() == 0) return null;

        int[] sg = new int[smoothingGroups.size()];
        for (int i = 0; i < smoothingGroups.size(); i++) sg[i] = smoothingGroups.get(i);
        mesh.getFaceSmoothingGroups().setAll(sg);

        MeshView fillView = new MeshView(mesh);
        fillView.setCullFace(CullFace.NONE);
        fillView.setDrawMode(DrawMode.FILL);
        fillView.setMaterial(createMaterial(color));

        return new Group(fillView);
    }

    // --- ANALYTIC SPHERE FAST PATH ---

    private Node buildAnalyticSphereIfPossible() {
        SphereInfo info = tryParseSphereEquation(normalize(rawText));
        if (info == null) return null;

        Sphere sphere = new Sphere(info.radius * SCALE, 128);
        sphere.setCullFace(CullFace.BACK);
        sphere.setMaterial(createMaterial(color));

        if (Math.abs(info.cx) > 1e-9) sphere.setTranslateX(info.cx * SCALE);
        if (Math.abs(info.cy) > 1e-9) sphere.setTranslateY(-info.cy * SCALE);
        if (Math.abs(info.cz) > 1e-9) sphere.setTranslateZ(info.cz * SCALE);

        return sphere;
    }

    private SphereInfo tryParseSphereEquation(String s) {
        if (s == null || !s.contains("=")) return null;
        String[] parts = s.split("=");
        if (parts.length != 2) return null;

        SphereInfo info = tryParseCenteredSphere(parts[0], parts[1]);
        if (info != null) return info;

        return tryParseCenteredSphere(parts[1], parts[0]);
    }

    // UPDATED: Now supports evaluating parameters dynamically!
    private SphereInfo tryParseCenteredSphere(String exprSide, String constantSide) {
        double constant;
        try {
            constant = Double.parseDouble(constantSide);
        } catch (Exception ex) {
            // If it's a parameter like 'a', evaluate it using current slider value
            try {
                Set<String> vars = extractVariables(constantSide);
                Expression e = new ExpressionBuilder(constantSide).variables(vars).build();
                for (String v : vars) {
                    e.setVariable(v, getParam(v, 1.0));
                }
                constant = e.evaluate();
            } catch (Exception e2) {
                return null;
            }
        }

        // Return null instead of crashing if slider goes negative
        if (constant < 0) return null;

        SphereInfo originSphere = parseOriginSphere(exprSide, constant);
        if (originSphere != null) return originSphere;

        return null;
    }

    private SphereInfo parseOriginSphere(String expr, double rhs) {
        String[] terms = expr.split("\\+");
        if (terms.length != 3) return null;

        Set<String> needed = new HashSet<>(Arrays.asList("x^2", "y^2", "z^2"));
        Set<String> found = new HashSet<>();

        for (String term : terms) {
            String t = term.trim();
            if (needed.contains(t)) {
                found.add(t);
            } else {
                return null;
            }
        }

        if (found.size() != 3) return null;

        double radius = Math.sqrt(rhs);
        if (Double.isNaN(radius) || Double.isInfinite(radius) || radius <= 0) return null;

        SphereInfo info = new SphereInfo();
        info.radius = radius;
        info.cx = 0;
        info.cy = 0;
        info.cz = 0;
        return info;
    }

    private static final class SphereInfo {
        double radius;
        double cx;
        double cy;
        double cz;
    }

    // --- GENERIC IMPLICIT SURFACE BUILDER ---

    private Node buildImplicitSurface() {
        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0, 0);

        double min = -10.0, max = 10.0, step = 0.16;
        int steps = (int) Math.ceil((max - min) / step);
        int N = steps + 1;

        double[][][] val = new double[N][N][N];

        for (int i = 0; i < N; i++) {
            if (Thread.currentThread().isInterrupted()) return null; // STOP IF CANCELLED

            double x = min + i * step;
            for (int j = 0; j < N; j++) {
                double y = min + j * step;
                for (int k = 0; k < N; k++) {
                    double z = min + k * step;
                    val[i][j][k] = eval(surfaceExpr, x, y, z, 0);
                }
            }
        }

        Map<Long, Integer> edgeCache = new HashMap<>();

        int[][] tets = { {0,1,2,6}, {0,2,3,6}, {0,3,7,6}, {0,7,4,6}, {0,4,5,6}, {0,5,1,6} };
        int[][] corners = { {0,0,0}, {1,0,0}, {1,1,0}, {0,1,0}, {0,0,1}, {1,0,1}, {1,1,1}, {0,1,1} };

        int[] cx = new int[8], cy = new int[8], cz = new int[8];

        for (int i = 0; i < steps; i++) {
            if (Thread.currentThread().isInterrupted()) return null; // STOP IF CANCELLED

            for (int j = 0; j < steps; j++) {
                for (int k = 0; k < steps; k++) {
                    for (int c = 0; c < 8; c++) {
                        cx[c] = i + corners[c][0];
                        cy[c] = j + corners[c][1];
                        cz[c] = k + corners[c][2];
                    }
                    for (int[] tet : tets) {
                        processTetrahedron(mesh, val, tet, cx, cy, cz, min, step, edgeCache, N);
                    }
                }
            }
        }

        if (mesh.getPoints().size() == 0 || mesh.getFaces().size() == 0) return null;

        int faceCount = mesh.getFaces().size() / 6;
        int[] smoothingGroups = new int[faceCount];
        Arrays.fill(smoothingGroups, 1);
        mesh.getFaceSmoothingGroups().addAll(smoothingGroups);

        MeshView view = new MeshView(mesh);
        view.setCullFace(CullFace.NONE);
        view.setDrawMode(DrawMode.FILL);
        view.setMaterial(createMaterial(color));
        return new Group(view);
    }

    private void processTetrahedron(TriangleMesh mesh, double[][][] val, int[] tet,
                                    int[] cx, int[] cy, int[] cz,
                                    double min, double step,
                                    Map<Long, Integer> edgeCache, int N) {
        int[] pos = new int[4];
        int[] neg = new int[4];
        int posCount = 0, negCount = 0;

        double[] v = new double[4];
        for (int i = 0; i < 4; i++) {
            int c = tet[i];
            v[i] = val[cx[c]][cy[c]][cz[c]];
            if (Double.isNaN(v[i])) return;
            if (v[i] >= 0) pos[posCount++] = i;
            else neg[negCount++] = i;
        }

        if (posCount == 0 || negCount == 0) return;

        if (posCount == 1 || posCount == 3) {
            boolean singlePos = (posCount == 1);
            int centerIdx = singlePos ? pos[0] : neg[0];

            int[] others = new int[3];
            int idx = 0;
            for (int i = 0; i < 4; i++) {
                if (i != centerIdx) others[idx++] = i;
            }

            int i1 = getEdgeVertex(mesh, val,
                    cx[tet[centerIdx]], cy[tet[centerIdx]], cz[tet[centerIdx]],
                    cx[tet[others[0]]], cy[tet[others[0]]], cz[tet[others[0]]],
                    min, step, edgeCache, N);

            int i2 = getEdgeVertex(mesh, val,
                    cx[tet[centerIdx]], cy[tet[centerIdx]], cz[tet[centerIdx]],
                    cx[tet[others[1]]], cy[tet[others[1]]], cz[tet[others[1]]],
                    min, step, edgeCache, N);

            int i3 = getEdgeVertex(mesh, val,
                    cx[tet[centerIdx]], cy[tet[centerIdx]], cz[tet[centerIdx]],
                    cx[tet[others[2]]], cy[tet[others[2]]], cz[tet[others[2]]],
                    min, step, edgeCache, N);

            addOrientedTriangle(mesh, i1, i2, i3);
        } else if (posCount == 2) {
            int p0 = pos[0], p1 = pos[1];
            int n0 = neg[0], n1 = neg[1];

            int e1 = getEdgeVertex(mesh, val,
                    cx[tet[p0]], cy[tet[p0]], cz[tet[p0]],
                    cx[tet[n0]], cy[tet[n0]], cz[tet[n0]],
                    min, step, edgeCache, N);

            int e2 = getEdgeVertex(mesh, val,
                    cx[tet[p1]], cy[tet[p1]], cz[tet[p1]],
                    cx[tet[n0]], cy[tet[n0]], cz[tet[n0]],
                    min, step, edgeCache, N);

            int e3 = getEdgeVertex(mesh, val,
                    cx[tet[p1]], cy[tet[p1]], cz[tet[p1]],
                    cx[tet[n1]], cy[tet[n1]], cz[tet[n1]],
                    min, step, edgeCache, N);

            int e4 = getEdgeVertex(mesh, val,
                    cx[tet[p0]], cy[tet[p0]], cz[tet[p0]],
                    cx[tet[n1]], cy[tet[n1]], cz[tet[n1]],
                    min, step, edgeCache, N);

            addOrientedTriangle(mesh, e1, e2, e3);
            addOrientedTriangle(mesh, e1, e3, e4);
        }
    }

    private void addOrientedTriangle(TriangleMesh mesh, int i1, int i2, int i3) {
        if (i1 < 0 || i2 < 0 || i3 < 0) return;

        Point3D p1 = meshPointToMath(mesh, i1);
        Point3D p2 = meshPointToMath(mesh, i2);
        Point3D p3 = meshPointToMath(mesh, i3);

        Point3D u = p2.subtract(p1);
        Point3D v = p3.subtract(p1);
        Point3D normal = u.crossProduct(v);

        Point3D centroid = new Point3D(
                (p1.getX() + p2.getX() + p3.getX()) / 3.0,
                (p1.getY() + p2.getY() + p3.getY()) / 3.0,
                (p1.getZ() + p2.getZ() + p3.getZ()) / 3.0
        );

        Point3D grad = gradientAt(centroid.getX(), centroid.getY(), centroid.getZ());

        double dot = normal.dotProduct(grad);

        if (Double.isNaN(dot) || dot >= 0) {
            mesh.getFaces().addAll(i1, 0, i2, 0, i3, 0);
        } else {
            mesh.getFaces().addAll(i1, 0, i3, 0, i2, 0);
        }
    }

    private Point3D meshPointToMath(TriangleMesh mesh, int index) {
        int b = index * 3;
        double x = mesh.getPoints().get(b) / SCALE;
        double y = mesh.getPoints().get(b + 1);
        double z = mesh.getPoints().get(b + 2) / SCALE;

        double mathX = x;
        double mathY = z;
        double mathZ = -y / SCALE;
        return new Point3D(mathX, mathY, mathZ);
    }

    private Point3D gradientAt(double x, double y, double z) {
        double h = 1e-3;

        double fx1 = eval(surfaceExpr, x + h, y, z, 0);
        double fx0 = eval(surfaceExpr, x - h, y, z, 0);
        double fy1 = eval(surfaceExpr, x, y + h, z, 0);
        double fy0 = eval(surfaceExpr, x, y - h, z, 0);
        double fz1 = eval(surfaceExpr, x, y, z + h, 0);
        double fz0 = eval(surfaceExpr, x, y, z - h, 0);

        double gx = (fx1 - fx0) / (2.0 * h);
        double gy = (fy1 - fy0) / (2.0 * h);
        double gz = (fz1 - fz0) / (2.0 * h);

        Point3D g = new Point3D(gx, gy, gz);
        if (g.magnitude() < 1e-12) return new Point3D(0, 1, 0);
        return g.normalize();
    }

    private int getEdgeVertex(TriangleMesh mesh, double[][][] val,
                              int x1, int y1, int z1, int x2, int y2, int z2,
                              double min, double step,
                              Map<Long, Integer> edgeCache, int N) {

        long p1Idx = x1 + (long) y1 * N + (long) z1 * N * N;
        long p2Idx = x2 + (long) y2 * N + (long) z2 * N * N;
        long edgeId = (Math.min(p1Idx, p2Idx) << 32) | Math.max(p1Idx, p2Idx);

        if (edgeCache.containsKey(edgeId)) return edgeCache.get(edgeId);

        double v1 = val[x1][y1][z1];
        double v2 = val[x2][y2][z2];

        double t = (Math.abs(v1 - v2) < 1e-9) ? 0.5 : -v1 / (v2 - v1);
        t = Math.max(0.0, Math.min(1.0, t));

        double px = min + (x1 + (x2 - x1) * t) * step;
        double py = min + (y1 + (y2 - y1) * t) * step;
        double pz = min + (z1 + (z2 - z1) * t) * step;

        int newIndex = mesh.getPoints().size() / 3;
        mesh.getPoints().addAll(
                (float) (px * SCALE),
                (float) (-pz * SCALE),
                (float) (py * SCALE)
        );

        edgeCache.put(edgeId, newIndex);
        return newIndex;
    }

    // --- UTILITIES ---

    private PhongMaterial createMaterial(Color base) {
        PhongMaterial mat = new PhongMaterial(base);
        mat.setSpecularColor(Color.TRANSPARENT);
        return mat;
    }

    private boolean isAllClamped(TriangleMesh mesh, int pA, int pB, int pC) {
        double limit = 10.25 * SCALE * 0.999;
        return (getPoint(mesh, pA).getY() <= -limit && getPoint(mesh, pB).getY() <= -limit && getPoint(mesh, pC).getY() <= -limit) ||
                (getPoint(mesh, pA).getY() >= limit && getPoint(mesh, pB).getY() >= limit && getPoint(mesh, pC).getY() >= limit);
    }

    private boolean tooFar(TriangleMesh mesh, int a, int b, int c) {
        Point3D p1 = getPoint(mesh, a), p2 = getPoint(mesh, b), p3 = getPoint(mesh, c);
        return p1.distance(p2) > 60 || p2.distance(p3) > 60 || p1.distance(p3) > 60;
    }

    private Point3D getPoint(TriangleMesh mesh, int index) {
        int b = index * 3;
        return new Point3D(mesh.getPoints().get(b), mesh.getPoints().get(b + 1), mesh.getPoints().get(b + 2));
    }

    private Node createSegment(double x1, double y1, double z1, double x2, double y2, double z2, Color color, double radius) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1e-9) return null;
        Cylinder line = new Cylinder(radius, length * SCALE);
        line.setMaterial(createMaterial(color));
        line.setTranslateX(((x1 + x2) / 2.0) * SCALE);
        line.setTranslateY(-((y1 + y2) / 2.0) * SCALE);
        line.setTranslateZ(((z1 + z2) / 2.0) * SCALE);
        Point3D diff = new Point3D(dx, -dy, dz);
        if (diff.magnitude() > 1e-6) {
            Point3D axis = new Point3D(0, 1, 0).crossProduct(diff);
            double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, new Point3D(0, 1, 0).dotProduct(diff.normalize())))));
            if (axis.magnitude() > 1e-6) line.getTransforms().add(new Rotate(-angle, axis));
        }
        return line;
    }

    private double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    private void ensureParams(Set<String> vars) {
        Set<String> currentValidParams = new HashSet<>();

        for (String v : vars) {
            if (!Arrays.asList("x", "y", "z", "t", "pi", "e").contains(v)) {
                currentValidParams.add(v);
                if (!params.containsKey(v)) {
                    params.put(v, 1.0);
                }
            }
        }
        params.keySet().retainAll(currentValidParams);
    }

    private Set<String> extractVariables(String expr) {
        Set<String> vars = new HashSet<>();
        Matcher m = VAR_PATTERN.matcher(expr);
        while (m.find()) {
            String t = m.group();
            if (!isReserved(t)) vars.add(t);
        }
        return vars;
    }

    private boolean isReserved(String t) {
        return Arrays.asList("sin", "cos", "tan", "asin", "acos", "atan", "sqrt", "abs", "log", "log10", "exp", "floor", "ceil", "pow", "min", "max", "pi", "e").contains(t);
    }

    private String normalize(String s) {
        return s.toLowerCase().replaceAll("\\s+", "");
    }

    private String[] splitTopLevel(String t, char d) {
        List<String> res = new ArrayList<>();
        StringBuilder curr = new StringBuilder();
        int dep = 0;
        for (char c : t.toCharArray()) {
            if (c == '(') dep++;
            if (c == ')') dep--;
            if (c == d && dep == 0) {
                res.add(curr.toString());
                curr.setLength(0);
            } else curr.append(c);
        }
        res.add(curr.toString());
        return res.toArray(new String[0]);
    }

    private double eval(Expression e, double x, double y, double z, double t) {
        if (e == null) return Double.NaN;
        try {
            e.setVariable("x", x);
            e.setVariable("y", y);
            e.setVariable("z", z);
            e.setVariable("t", t);
            for (var entry : params.entrySet()) {
                if (!Arrays.asList("x", "y", "z", "t").contains(entry.getKey())) {
                    e.setVariable(entry.getKey(), entry.getValue());
                }
            }
            return e.evaluate();
        } catch (Exception ex) {
            return Double.NaN;
        }
    }

    private boolean bad(double v) {
        return Double.isNaN(v) || Double.isInfinite(v);
    }

    // --- Getters / Setters ---
    public String getRawText() { return rawText; }

    public void setRawText(String rawText) {
        this.rawText = rawText == null ? "" : rawText.trim();
        rebuild();
    }

    public Color getColor() { return color; }
    public void setColor(Color color) { if (color != null) this.color = color; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public Type getType() { return type; }

    public Set<String> getParamNames() {
        return params.keySet();
    }

    public double getParam(String name, double defaultValue) {
        return params.getOrDefault(name, defaultValue);
    }

    public void setParam(String name, double value) {
        params.put(name, value);
    }
}