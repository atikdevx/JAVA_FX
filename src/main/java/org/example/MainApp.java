package com.equationplotter;

import com.equationplotter.ui.HomeView;
import com.equationplotter.ui.SplashView;
import com.equationplotter.ui.Workspace3DView;
import com.equationplotter.ui.WorkspacePolarView; // <-- 1. ADDED IMPORT
import com.equationplotter.ui.WorkspaceView;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    private Stage stage;
    private WorkspaceView workspace;
    private Workspace3DView workspace3D;
    private WorkspacePolarView workspacePolar; // <-- 2. ADDED FIELD

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        stage.setTitle("Pika Plotter");
        stage.setMinWidth(1152);
        stage.setMinHeight(945);

        SplashView splashView = new SplashView(this::showHome);

        Scene scene = new Scene(splashView, 1152, 945, true);
        stage.setScene(scene);
        stage.show();
    }

    private void switchRootSmooth(Parent newRoot) {
        if (stage.getScene() == null) {
            stage.setScene(new Scene(newRoot, 1152, 945, true));
            return;
        }

        newRoot.setOpacity(0);
        stage.getScene().setRoot(newRoot);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.75), newRoot);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void showHome() {
        // <-- 3. ADDED 'this::showWorkspacePolar' to the constructor
        HomeView home = new HomeView(this::showWorkspace, this::showWorkspace3D, this::showWorkspacePolar);
        switchRootSmooth(home);
    }

    private void showWorkspace() {
        if (workspace == null) {
            workspace = new WorkspaceView(this::showHome);
        }
        switchRootSmooth(workspace);
    }

    private void showWorkspace3D() {
        if (workspace3D == null) {
            workspace3D = new Workspace3DView(this::showHome);
        }
        switchRootSmooth(workspace3D);
    }

    // <-- 4. ADDED THIS WHOLE METHOD
    private void showWorkspacePolar() {
        if (workspacePolar == null) {
            workspacePolar = new WorkspacePolarView(this::showHome);
        }
        switchRootSmooth(workspacePolar);
    }

    public static void main(String[] args) {
        launch(args);
    }
}