package com.equationplotter;

import com.equationplotter.ui.HomeView;
import com.equationplotter.ui.WorkspaceView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage stage;

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
        Scene scene = new Scene(home, 1152, 945);
        stage.setScene(scene);
    }

    private void showWorkspace() {
        // 🔥 Back button এখান থেকে Home এ ফিরবে
        WorkspaceView workspace = new WorkspaceView(this::showHome);
        Scene scene = new Scene(workspace);
        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
