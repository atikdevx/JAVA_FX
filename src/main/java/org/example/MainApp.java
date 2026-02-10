package com.equationplotter;

import com.equationplotter.ui.HomeView;
import com.equationplotter.ui.WorkspaceView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
 // atik is a good boy
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
        WorkspaceView workspace = new WorkspaceView();
        Scene scene = new Scene(workspace, 1152, 945);
        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
