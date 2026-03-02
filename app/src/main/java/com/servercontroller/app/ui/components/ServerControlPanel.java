package com.servercontroller.app.ui.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ServerControlPanel {
    private final VBox root;
    private final TextField message;
    private final Button sendButton;
    private final Button restartButton;
    private final Button stopButton;

    public ServerControlPanel() {
        root = new VBox(12);
        root.getStyleClass().add("panel");
        root.setPadding(new Insets(12));

        Label title = new Label("Control");
        title.getStyleClass().add("panel-title");

        HBox broadcast = new HBox(8);
        message = new TextField();
        message.setPromptText("Broadcast message");
        sendButton = new Button("Send");
        sendButton.getStyleClass().add("primary-button");
        broadcast.getChildren().addAll(message, sendButton);

        HBox actions = new HBox(8);
        restartButton = new Button("Restart");
        stopButton = new Button("Stop");
        restartButton.getStyleClass().add("warning-button");
        stopButton.getStyleClass().add("danger-button");
        actions.getChildren().addAll(restartButton, stopButton);

        root.getChildren().addAll(title, broadcast, actions);
    }

    public Node node() {
        return root;
    }

    public TextField messageField() {
        return message;
    }

    public Button sendButton() {
        return sendButton;
    }

    public Button restartButton() {
        return restartButton;
    }

    public Button stopButton() {
        return stopButton;
    }
}
