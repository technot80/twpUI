package com.servercontroller.app;

import com.servercontroller.app.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerControllerApp extends Application {
    @Override
    public void start(Stage stage) {
        MainWindow mainWindow = new MainWindow();
        Scene scene = new Scene(mainWindow.root(), 1200, 800);
        scene.getStylesheets().add(MainWindow.class.getResource("/com/servercontroller/app/ui/styles/app.css").toExternalForm());

        stage.setTitle("ServerController");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
