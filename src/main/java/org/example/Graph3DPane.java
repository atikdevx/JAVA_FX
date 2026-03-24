package com.equationplotter.ui;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.List;

public class Graph3DPane extends StackPane {
    private final Group root3D = new Group();
    private final Group world = new Group();
    private final Group graphGroup = new Group();
    private final Group boxGroup = new Group();
    private final Group gridGroup = new Group();
    private final Group axesGroup = new Group();
    private final Group axisLabelsGroup=new Group();
    private double currentLabelStep=-1;
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final SubScene subScene;

    private final Rotate rotateX = new Rotate(35, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(45, Rotate.Y_AXIS);
    private final Translate worldTranslate = new Translate(0, 0, 0);

    private double anchorX;
    private double anchorY;
    private double anchorAngleX;
    private double anchorAngleY;
    private MeshView createArrowHead(double sizeDouble, double lengthDouble, PhongMaterial mat) {
        // Cast the doubles to floats for the TriangleMesh
        float size = (float) sizeDouble;
        float length = (float) lengthDouble;

        TriangleMesh mesh = new TriangleMesh();

        // Create the 5 points of a pyramid (using floats)
        mesh.getPoints().addAll(
                0f, 0f, length,         // 0: tip
                -size, -size, 0f,       // 1: base corner
                size, -size, 0f,       // 2: base corner
                size,  size, 0f,       // 3: base corner
                -size,  size, 0f        // 4: base corner
        );

        // Dummy texture coordinates
        mesh.getTexCoords().addAll(0f, 0f);

        // Connect the points to form faces
        mesh.getFaces().addAll(
                0,0, 2,0, 1,0,
                0,0, 3,0, 2,0,
                0,0, 4,0, 3,0,
                0,0, 1,0, 4,0,
                1,0, 2,0, 3,0,
                1,0, 3,0, 4,0
        );

        MeshView view = new MeshView(mesh);
        view.setMaterial(mat);
        view.setCullFace(CullFace.NONE);
        return view;
    }
    private final List<Plot3DDefinition> plots = new ArrayList<>();

    public Graph3DPane() {

        setStyle("-fx-background-color: #f7f7f7;");

        world.getTransforms().addAll(rotateX, rotateY, worldTranslate);

        buildGrid();
        buildAxes();
        buildBoundingBox();
        buildLights();

        graphGroup.setDepthTest(DepthTest.ENABLE);
        world.setDepthTest(DepthTest.ENABLE);

        world.getChildren().addAll(boxGroup, gridGroup, axesGroup, graphGroup);
        root3D.getChildren().add(world);

        camera.setNearClip(0.1);
        camera.setFarClip(30000);
        camera.setTranslateZ(-2000);
        camera.setFieldOfView(28);

        subScene = new SubScene(root3D, 1152, 945, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#f7f7f7"));
        subScene.setCamera(camera);
        subScene.setManaged(false);

        getChildren().add(subScene);

        widthProperty().addListener((obs, o, n) -> subScene.setWidth(n.doubleValue()));
        heightProperty().addListener((obs, o, n) -> subScene.setHeight(n.doubleValue()));

        enableMouseControls();
        updateAxisMarkings(Math.abs(camera.getTranslateZ()));
        camera.translateZProperty().addListener((obs, oldVal, newVal) -> {
            updateAxisMarkings(Math.abs(newVal.doubleValue()));
        });
    }

    private void buildLights() {
        AmbientLight ambient = new AmbientLight(Color.rgb(130, 130, 130));
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(1500);
        pointLight.setTranslateY(-2000);
        pointLight.setTranslateZ(-3000);
        root3D.getChildren().addAll(ambient, pointLight);
    }

    private void buildGrid() {
        gridGroup.getChildren().clear();
        double size = 640;
        double step = 40;
        PhongMaterial minor = new PhongMaterial(Color.web("#555555"));
        PhongMaterial major = new PhongMaterial(Color.web("#2f2f2f"));
        for (double i = -size; i <= size; i += step) {
            boolean isMajor = Math.round(i / step) % 5 == 0;
            Box lineX = new Box(size * 2, 0.6, 0.6);
            lineX.setTranslateZ(i);
            lineX.setMaterial(isMajor ? major : minor);
            Box lineZ = new Box(0.6, 0.6, size * 2);
            lineZ.setTranslateX(i);
            lineZ.setMaterial(isMajor ? major : minor);
            gridGroup.getChildren().addAll(lineX, lineZ);
        }
    }
    private void buildAxes() {
        axesGroup.getChildren().clear();
        PhongMaterial axisMat = new PhongMaterial(Color.web("#111111"));

        // --- 1. Lines ---
        Box xAxis = new Box(820, 3, 3);
        xAxis.setMaterial(axisMat);

        Box yAxis = new Box(3, 820, 3);
        yAxis.setTranslateY(-410);
        yAxis.setMaterial(axisMat);

        Box zAxis = new Box(3, 3, 820);
        zAxis.setMaterial(axisMat);

        // --- 2. 3D Arrowheads ---
        // X Arrow (Rotate to point right)
        MeshView xArrow = createArrowHead(8, 24, axisMat);
        xArrow.setTranslateX(410);
        xArrow.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));

        // Y Arrow (Rotate to point up)
        MeshView yArrow = createArrowHead(8, 24, axisMat);
        yArrow.setTranslateY(-820);
        yArrow.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

        // Z Arrow (Points forward by default)
        MeshView zArrow = createArrowHead(8, 24, axisMat);
        zArrow.setTranslateZ(410);

        // --- 3. Text Labels ---
        Font labelFont = Font.font("Arial", FontWeight.BOLD, 24);

        Text xLabel = new Text("X");
        xLabel.setFont(labelFont);
        xLabel.setTranslateX(445); // Pushed further out to clear the arrowhead
        xLabel.setTranslateY(-10);

        Text yLabel = new Text("Y");
        yLabel.setFont(labelFont);
        yLabel.setTranslateY(-850); // Pushed further out to clear the arrowhead
        yLabel.setTranslateX(15);

        Text zLabel = new Text("Z");
        zLabel.setFont(labelFont);
        zLabel.setTranslateZ(445); // Pushed further out to clear the arrowhead
        zLabel.setTranslateY(-10);

        // Add everything to the group
        axesGroup.getChildren().addAll(
                xAxis, yAxis, zAxis,
                xArrow, yArrow, zArrow,
                xLabel, yLabel, zLabel,
                axisLabelsGroup
        );
    }
    private void updateAxisMarkings(double zDistance) {
        // Your graph maps 1 Math Unit to 40 JavaFX Pixels (1 grid square)
        final double SCALE = 40.0;

        // 1. Determine tick spacing based on zoom
        int step;
        if (zDistance < 1000) step = 40;       // Every 40 pixels (1 math unit)
        else if (zDistance < 2000) step = 80;  // Every 80 pixels (2 math units)
        else if (zDistance < 3500) step = 200; // Every 200 pixels (5 math units)
        else step = 400;                       // Every 400 pixels (10 math units)

        if (step == currentLabelStep) return;
        currentLabelStep = step;

        axisLabelsGroup.getChildren().clear();
        Font tickFont = Font.font("Arial", FontWeight.NORMAL, 14);

        // 2. Generate X and Z labels
        for (int i = -400; i <= 400; i += step) {
            if (i == 0) continue; // Skip origin

            // Calculate actual math coordinate and format it cleanly (drop .0 if whole number)
            double mathValue = i / SCALE;
            String labelText = (mathValue == (long) mathValue) ?
                    String.valueOf((long) mathValue) :
                    String.valueOf(mathValue);

            // X-Axis
            Text xText = new Text(labelText);
            xText.setFont(tickFont);
            xText.setFill(Color.DARKBLUE);
            xText.setTranslateX(i);
            xText.setTranslateY(15);
            xText.setTranslateZ(0);

            // Z-Axis
            Text zText = new Text(labelText);
            zText.setFont(tickFont);
            zText.setFill(Color.DARKRED);
            zText.setTranslateX(15);
            zText.setTranslateY(15);
            zText.setTranslateZ(i);

            axisLabelsGroup.getChildren().addAll(xText, zText);
        }

        // 3. Generate Y labels (Vertical)
        for (int i = -step; i >= -800; i -= step) {
            // Calculate math coordinate
            double mathValue = Math.abs(i) / SCALE;
            String labelText = (mathValue == (long) mathValue) ?
                    String.valueOf((long) mathValue) :
                    String.valueOf(mathValue);

            Text yText = new Text(labelText);
            yText.setFont(tickFont);
            yText.setFill(Color.DARKGREEN);
            yText.setTranslateX(15);
            yText.setTranslateY(i);
            yText.setTranslateZ(0);

            axisLabelsGroup.getChildren().add(yText);
        }
    }

    private void buildBoundingBox() {
        boxGroup.getChildren().clear();
        double halfW = 400;
        double height = 820;
        double halfD = 400;
        double topY = -height / 2.0;
        double bottomY = height / 2.0;
        PhongMaterial boxMat = new PhongMaterial(Color.web("#bfbfbf"));
        addEdge(-halfW, topY, -halfD, halfW, topY, -halfD, boxMat);
        addEdge(halfW, topY, -halfD, halfW, topY, halfD, boxMat);
        addEdge(halfW, topY, halfD, -halfW, topY, halfD, boxMat);
        addEdge(-halfW, topY, halfD, -halfW, topY, -halfD, boxMat);
        addEdge(-halfW, bottomY, -halfD, halfW, bottomY, -halfD, boxMat);
        addEdge(halfW, bottomY, -halfD, halfW, bottomY, halfD, boxMat);
        addEdge(halfW, bottomY, halfD, -halfW, bottomY, halfD, boxMat);
        addEdge(-halfW, bottomY, halfD, -halfW, bottomY, -halfD, boxMat);
        addEdge(-halfW, topY, -halfD, -halfW, bottomY, -halfD, boxMat);
        addEdge(halfW, topY, -halfD, halfW, bottomY, -halfD, boxMat);
        addEdge(halfW, topY, halfD, halfW, bottomY, halfD, boxMat);
        addEdge(-halfW, topY, halfD, -halfW, bottomY, halfD, boxMat);
    }

    private void addEdge(double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         PhongMaterial mat) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        Box line = new Box(0.8, 0.8, length);
        line.setMaterial(mat);
        line.setTranslateX((x1 + x2) / 2);
        line.setTranslateY((y1 + y2) / 2);
        line.setTranslateZ((z1 + z2) / 2);
        Point3D from = new Point3D(0, 0, 1);
        Point3D to = new Point3D(dx, dy, dz).normalize();
        Point3D axis = from.crossProduct(to);
        double angle = Math.toDegrees(Math.acos(from.dotProduct(to)));
        if (axis.magnitude() > 1e-6)
            line.getTransforms().add(new Rotate(angle, axis));
        boxGroup.getChildren().add(line);
    }

    private void enableMouseControls() {
        subScene.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            anchorX = e.getSceneX();
            anchorY = e.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });
        subScene.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            rotateX.setAngle(anchorAngleX - (e.getSceneY() - anchorY) * 0.28);
            rotateY.setAngle(anchorAngleY + (e.getSceneX() - anchorX) * 0.28);
            if (rotateX.getAngle() > 88) rotateX.setAngle(88);
            if (rotateX.getAngle() < -88) rotateX.setAngle(-88);
        });
        subScene.addEventHandler(ScrollEvent.SCROLL, e -> {
            double next = camera.getTranslateZ() + e.getDeltaY() * 0.75;
            if (next > -220) next = -220;
            if (next < -4500) next = -4500;
            camera.setTranslateZ(next);
        });
    }

    public void setPlots(List<Plot3DDefinition> definitions) {
        plots.clear();
        if (definitions != null) plots.addAll(definitions);
        rebuildGraph();
    }

    public void resetCamera() {
        rotateX.setAngle(35);
        rotateY.setAngle(45);
        camera.setTranslateZ(-2000);
        worldTranslate.setX(0);
        worldTranslate.setY(0);
        worldTranslate.setZ(0);
    }

    private void rebuildGraph() {
        graphGroup.getChildren().clear();
        for (Plot3DDefinition def : plots) {
            if (def == null || !def.isVisible()) continue;
            Node node = def.buildNode();
            if (node != null) {
                node.setDepthTest(DepthTest.ENABLE);
                graphGroup.getChildren().add(node);
            }
        }
    }
}