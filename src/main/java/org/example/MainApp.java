package com.equationplotter;

import com.equationplotter.ui.HomeView;
import com.equationplotter.ui.WorkspaceView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage stage;
    // Keep a single instance of WorkspaceView to preserve state
    private WorkspaceView workspace;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Pika Plotter");
        stage.setMinWidth(1152);
        stage.setMinHeight(945);

        showHome();
        stage.show();
    }

    private void showHome() {
        HomeView home = new HomeView(this::showWorkspace);

        if (stage.getScene() == null) {
            Scene scene = new Scene(home, 1152, 945);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(home);
        }
    }

    private void showWorkspace() {
        // [FIXED] Create workspace only once
        if (workspace == null) {
            workspace = new WorkspaceView(this::showHome);
        }

        if (stage.getScene() == null) {
            Scene scene = new Scene(workspace, 1152, 945);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(workspace);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}









