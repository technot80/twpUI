package com.servercontroller.app.ui.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerLogPanel {
    private final VBox root;
    private final TextArea logArea;
    private final ToggleButton errorToggle;
    private final ToggleButton warnToggle;
    private final ToggleButton infoToggle;
    private final ToggleButton debugToggle;

    public ServerLogPanel() {
        root = new VBox(12);
        root.getStyleClass().add("panel");
        root.setPadding(new Insets(12));

        Label title = new Label("Logs");
        title.getStyleClass().add("panel-title");

        HBox filters = new HBox(8);
        errorToggle = new ToggleButton("ERROR");
        warnToggle = new ToggleButton("WARN");
        infoToggle = new ToggleButton("INFO");
        debugToggle = new ToggleButton("DEBUG");
        errorToggle.setSelected(true);
        warnToggle.setSelected(true);
        infoToggle.setSelected(true);
        debugToggle.setSelected(false);

        filters.getChildren().addAll(errorToggle, warnToggle, infoToggle, debugToggle);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("log-area");

        root.getChildren().addAll(title, filters, logArea);
    }

    public Node node() {
        return root;
    }

    public void appendLog(String line) {
        logArea.appendText(line + "\n");
    }

    public List<String> selectedLogLevels() {
        return Stream.of(errorToggle, warnToggle, infoToggle, debugToggle)
                .filter(ToggleButton::isSelected)
                .map(ToggleButton::getText)
                .collect(Collectors.toList());
    }

    public ToggleButton errorToggle() {
        return errorToggle;
    }

    public ToggleButton warnToggle() {
        return warnToggle;
    }

    public ToggleButton infoToggle() {
        return infoToggle;
    }

    public ToggleButton debugToggle() {
        return debugToggle;
    }
}
