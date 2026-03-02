package com.servercontroller.app.ui.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ConnectionForm {
    private final VBox root;
    private final TextField nameField;
    private final TextField hostField;
    private final TextField portField;
    private final PasswordField apiKeyField;
    private final Button addButton;

    public ConnectionForm() {
        root = new VBox(12);
        root.getStyleClass().add("panel");
        root.setPadding(new Insets(12));

        Label title = new Label("Add server");
        title.getStyleClass().add("panel-title");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        nameField = new TextField();
        nameField.setPromptText("Name");
        hostField = new TextField();
        hostField.setPromptText("Host");
        portField = new TextField("8765");
        portField.setPromptText("Port");
        apiKeyField = new PasswordField();
        apiKeyField.setPromptText("API Key");

        grid.add(new Label("Name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Host"), 0, 1);
        grid.add(hostField, 1, 1);
        grid.add(new Label("Port"), 0, 2);
        grid.add(portField, 1, 2);
        grid.add(new Label("API Key"), 0, 3);
        grid.add(apiKeyField, 1, 3);

        addButton = new Button("Add Server");
        addButton.getStyleClass().add("primary-button");

        root.getChildren().addAll(title, grid, addButton);
    }

    public Node node() {
        return root;
    }

    public Button addButton() {
        return addButton;
    }

    public TextField nameField() {
        return nameField;
    }

    public TextField hostField() {
        return hostField;
    }

    public TextField portField() {
        return portField;
    }

    public PasswordField apiKeyField() {
        return apiKeyField;
    }
}
