package com.servercontroller.app.ui;

import com.servercontroller.app.ui.tabs.ServersTabPane;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainWindow {
    private final BorderPane root;

    public MainWindow() {
        root = new BorderPane();
        root.setTop(buildHeader());
        root.setCenter(new ServersTabPane().node());
        root.getStyleClass().add("root");
    }

    public Parent root() {
        return root;
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setPadding(new Insets(16));
        Label title = new Label("ServerController");
        title.getStyleClass().add("header-title");
        header.getChildren().add(title);
        return header;
    }
}
