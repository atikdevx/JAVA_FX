












package com.equationplotter.ui;

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

    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final SubScene subScene;

    private final Rotate rotateX = new Rotate(-22, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-30, Rotate.Y_AXIS);
    private final Translate worldTranslate = new Translate(0, 0, 0);

    private double anchorX;
    private double anchorY;
    private double anchorAngleX;
    private double anchorAngleY;

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
        camera.setTranslateZ(-1100);
        camera.setFieldOfView(28);

        subScene = new SubScene(root3D, 1152, 945, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#f7f7f7"));
        subScene.setCamera(camera);
        subScene.setManaged(false);

        getChildren().add(subScene);

        widthProperty().addListener((obs, oldVal, newVal) -> subScene.setWidth(newVal.doubleValue()));
        heightProperty().addListener((obs, oldVal, newVal) -> subScene.setHeight(newVal.doubleValue()));

        enableMouseControls();
    }

    private void buildLights() {
        AmbientLight ambient = new AmbientLight(Color.color(1, 1, 1, 0.95));

        PointLight key = new PointLight(Color.WHITE);
        key.setTranslateX(-500);
        key.setTranslateY(-450);
        key.setTranslateZ(-900);

        PointLight fill = new PointLight(Color.WHITE);
        fill.setTranslateX(550);
        fill.setTranslateY(-180);
        fill.setTranslateZ(-450);

        PointLight rim = new PointLight(Color.WHITE);
        rim.setTranslateX(0);
        rim.setTranslateY(350);
        rim.setTranslateZ(-250);

        root3D.getChildren().addAll(ambient, key, fill, rim);
    }






    private void buildGrid() {
        gridGroup.getChildren().clear();

        double size = 640;
        double step = 40;

        // Darker grid colors (visible on white)
        PhongMaterial minor = new PhongMaterial(Color.web("#555555"));
        PhongMaterial major = new PhongMaterial(Color.web("#2f2f2f"));

        for (double i = -size; i <= size; i += step) {
            boolean isMajor = Math.round(i / step) % 5 == 0;

            Box lineX = new Box(size * 2, 0.6, 0.6);   // slightly thicker
            lineX.setTranslateX(0);
            lineX.setTranslateY(0);
            lineX.setTranslateZ(i);
            lineX.setMaterial(isMajor ? major : minor);

            Box lineZ = new Box(0.6, 0.6, size * 2);
            lineZ.setTranslateX(i);
            lineZ.setTranslateY(0);
            lineZ.setTranslateZ(0);
            lineZ.setMaterial(isMajor ? major : minor);

            gridGroup.getChildren().addAll(lineX, lineZ);
        }
    }

//    private void buildGrid() {
//        gridGroup.getChildren().clear();
//
//        double size = 640;
//        double step = 40;
//
//        PhongMaterial minor = new PhongMaterial(Color.web("#dddddd"));
//        PhongMaterial major = new PhongMaterial(Color.web("#c8c8c8"));
//
//        for (double i = -size; i <= size; i += step) {
//            boolean isMajor = Math.round(i / step) % 5 == 0;
//
//            Box lineX = new Box(size * 2, 0.22, 0.22);
//            lineX.setTranslateX(0);
//            lineX.setTranslateY(0);
//            lineX.setTranslateZ(i);
//            lineX.setMaterial(isMajor ? major : minor);
//
//            Box lineZ = new Box(0.22, 0.22, size * 2);
//            lineZ.setTranslateX(i);
//            lineZ.setTranslateY(0);
//            lineZ.setTranslateZ(0);
//            lineZ.setMaterial(isMajor ? major : minor);
//
//            gridGroup.getChildren().addAll(lineX, lineZ);
//        }
//    }




    private void buildAxes() {
        axesGroup.getChildren().clear();

        // Dark axis color
        PhongMaterial axisMat = new PhongMaterial(Color.web("#111111"));

        Box xAxis = new Box(820, 3.0, 3.0);
        xAxis.setMaterial(axisMat);

        Box yAxis = new Box(3.0, 820, 3.0);
        yAxis.setTranslateY(-410);
        yAxis.setMaterial(axisMat);

        Box zAxis = new Box(3.0, 3.0, 820);
        zAxis.setMaterial(axisMat);

        axesGroup.getChildren().addAll(xAxis, yAxis, zAxis);
    }

//    private void buildAxes() {
//        axesGroup.getChildren().clear();
//
//        PhongMaterial axisMat = new PhongMaterial(Color.web("#8f8f8f"));
//
//        Box xAxis = new Box(820, 2.0, 2.0);
//        xAxis.setMaterial(axisMat);
//
//        Box yAxis = new Box(2.0, 820, 2.0);
//        yAxis.setTranslateY(-410);
//        yAxis.setMaterial(axisMat);
//
//        Box zAxis = new Box(2.0, 2.0, 820);
//        zAxis.setMaterial(axisMat);
//
//        axesGroup.getChildren().addAll(xAxis, yAxis, zAxis);
//    }

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
                         PhongMaterial material) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        Box line = new Box(0.8, 0.8, length);
        line.setMaterial(material);

        double midX = (x1 + x2) / 2.0;
        double midY = (y1 + y2) / 2.0;
        double midZ = (z1 + z2) / 2.0;

        line.setTranslateX(midX);
        line.setTranslateY(midY);
        line.setTranslateZ(midZ);

        Point3D from = new Point3D(0, 0, 1);
        Point3D to = new Point3D(dx, dy, dz).normalize();
        Point3D axis = from.crossProduct(to);

        double dot = from.dotProduct(to);
        dot = Math.max(-1.0, Math.min(1.0, dot));
        double angle = Math.toDegrees(Math.acos(dot));

        if (axis.magnitude() > 1e-6 && !Double.isNaN(angle)) {
            line.getTransforms().add(new Rotate(angle, axis));
        }

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
        if (definitions != null) {
            plots.addAll(definitions);
        }
        rebuildGraph();
    }

    public void resetCamera() {
        rotateX.setAngle(-22);
        rotateY.setAngle(-30);
        camera.setTranslateZ(-1100);
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