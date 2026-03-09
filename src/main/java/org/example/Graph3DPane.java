package com.equationplotter.ui;

import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

public class Graph3DPane extends StackPane {

    private final Group root3D = new Group();
    private final Group world = new Group();
    private final Group graphGroup = new Group();

    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final SubScene subScene;

    private final Rotate rotateX = new Rotate(-28, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-35, Rotate.Y_AXIS);

    private double anchorX;
    private double anchorY;
    private double anchorAngleX;
    private double anchorAngleY;

    private final List<Plot3DDefinition> plots = new ArrayList<>();

    public Graph3DPane() {
        setStyle("-fx-background-color: white;");

        buildWorld();
        buildLights();

        subScene = new SubScene(root3D, 1152, 945, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.WHITE);
        subScene.setCamera(camera);

        camera.setNearClip(0.1);
        camera.setFarClip(20000);
        camera.setTranslateZ(-1050);
        camera.setFieldOfView(32);

        getChildren().add(subScene);

        widthProperty().addListener((obs, oldVal, newVal) -> subScene.setWidth(newVal.doubleValue()));
        heightProperty().addListener((obs, oldVal, newVal) -> subScene.setHeight(newVal.doubleValue()));

        enableMouseControls();
    }

    private void buildWorld() {
        world.getTransforms().addAll(rotateX, rotateY);

        Group grid = createFloorGrid(16, 40);
        Group axes = createAxes();

        world.getChildren().addAll(grid, axes, graphGroup);
        root3D.getChildren().add(world);
    }

    private void buildLights() {
        AmbientLight ambient = new AmbientLight(Color.color(1, 1, 1, 0.92));

        PointLight light1 = new PointLight(Color.WHITE);
        light1.setTranslateX(-600);
        light1.setTranslateY(-500);
        light1.setTranslateZ(-900);

        PointLight light2 = new PointLight(Color.WHITE);
        light2.setTranslateX(500);
        light2.setTranslateY(-250);
        light2.setTranslateZ(-500);

        root3D.getChildren().addAll(ambient, light1, light2);
    }

    private Group createFloorGrid(int halfCount, double gap) {
        Group grid = new Group();

        PhongMaterial minorMat = new PhongMaterial(Color.web("#e9e9e9"));
        PhongMaterial majorMat = new PhongMaterial(Color.web("#d0d0d0"));

        double full = halfCount * gap;

        for (int i = -halfCount; i <= halfCount; i++) {
            boolean major = (i % 5 == 0);

            Box lineX = new Box(full * 2, 0.4, 0.4);
            lineX.setTranslateX(0);
            lineX.setTranslateY(0);
            lineX.setTranslateZ(i * gap);
            lineX.setMaterial(major ? majorMat : minorMat);

            Box lineZ = new Box(0.4, 0.4, full * 2);
            lineZ.setTranslateX(i * gap);
            lineZ.setTranslateY(0);
            lineZ.setTranslateZ(0);
            lineZ.setMaterial(major ? majorMat : minorMat);

            grid.getChildren().addAll(lineX, lineZ);
        }

        return grid;
    }

    private Group createAxes() {
        Group axes = new Group();

        PhongMaterial xMat = new PhongMaterial(Color.web("#222222"));
        PhongMaterial yMat = new PhongMaterial(Color.web("#222222"));
        PhongMaterial zMat = new PhongMaterial(Color.web("#222222"));

        Box xAxis = new Box(1400, 1.8, 1.8);
        xAxis.setMaterial(xMat);

        Box yAxis = new Box(1.8, 700, 1.8);
        yAxis.setTranslateY(-350);
        yAxis.setMaterial(yMat);

        Box zAxis = new Box(1.8, 1.8, 1400);
        zAxis.setMaterial(zMat);

        axes.getChildren().addAll(xAxis, yAxis, zAxis);
        return axes;
    }

    private void enableMouseControls() {
        subScene.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            anchorX = e.getSceneX();
            anchorY = e.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });

        subScene.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            rotateX.setAngle(anchorAngleX - (e.getSceneY() - anchorY) * 0.35);
            rotateY.setAngle(anchorAngleY + (e.getSceneX() - anchorX) * 0.35);

            if (rotateX.getAngle() > 89) rotateX.setAngle(89);
            if (rotateX.getAngle() < -89) rotateX.setAngle(-89);
        });

        subScene.addEventHandler(ScrollEvent.SCROLL, e -> {
            double next = camera.getTranslateZ() + e.getDeltaY() * 0.8;
            if (next > -180) next = -180;
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
        rotateX.setAngle(-28);
        rotateY.setAngle(-35);
        camera.setTranslateZ(-1050);
    }

    private void rebuildGraph() {
        graphGroup.getChildren().clear();

        for (Plot3DDefinition def : plots) {
            if (def == null || !def.isVisible()) continue;

            Node node = def.buildNode();
            if (node != null) {
                graphGroup.getChildren().add(node);
            }
        }
    }
}