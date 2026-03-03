package com.servercontroller.app.ui.components;

import com.servercontroller.app.ui.viewmodel.ServerTabViewModel;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MetricsPanel {
    private final VBox root;
    private final Label tpsValue;
    private final Label msptValue;
    private final Label cpuValue;
    private final Label memoryValue;
    private final Label playersValue;
    private final Label entitiesValue;

    public MetricsPanel() {
        root = new VBox(12);
        root.getStyleClass().add("panel");
        root.setPadding(new Insets(12));
        Label title = new Label("Metrics");
        title.getStyleClass().add("panel-title");
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        tpsValue = new Label("-");
        msptValue = new Label("-");
        cpuValue = new Label("-");
        memoryValue = new Label("-");
        playersValue = new Label("-");
        entitiesValue = new Label("-");

        grid.add(metricCard("TPS", tpsValue), 0, 0);
        grid.add(metricCard("MSPT", msptValue), 1, 0);
        grid.add(metricCard("CPU", cpuValue), 2, 0);
        grid.add(metricCard("Memory", memoryValue), 0, 1);
        grid.add(metricCard("Players", playersValue), 1, 1);
        grid.add(metricCard("Entities", entitiesValue), 2, 1);

        root.getChildren().addAll(title, grid);
    }

    public Node node() {
        return root;
    }

    public void bind(ServerTabViewModel viewModel) {
        tpsValue.textProperty().bind(viewModel.tpsProperty());
        msptValue.textProperty().bind(viewModel.msptProperty());
        cpuValue.textProperty().bind(viewModel.cpuProperty());
        memoryValue.textProperty().bind(viewModel.memoryProperty());
        playersValue.textProperty().bind(viewModel.playersProperty());
        entitiesValue.textProperty().bind(viewModel.entitiesProperty());
    }

    private HBox metricCard(String label, Label valueNode) {
        HBox card = new HBox();
        card.getStyleClass().add("metric-card");
        card.setPadding(new Insets(12));
        VBox content = new VBox(4);
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("metric-label");
        valueNode.getStyleClass().add("metric-value");
        content.getChildren().addAll(labelNode, valueNode);
        card.getChildren().add(content);
        return card;
    }
}
