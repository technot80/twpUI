package com.servercontroller.app.ui.components;

import com.servercontroller.app.ui.model.PluginMetricRow;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class PluginMetricsPanel {
    private final VBox root;
    private final TableView<PluginMetricRow> table;

    public PluginMetricsPanel() {
        root = new VBox(12);
        root.getStyleClass().add("panel");
        root.setPadding(new Insets(12));

        Label title = new Label("Plugin Metrics");
        title.getStyleClass().add("panel-title");

        table = new TableView<>();

        TableColumn<PluginMetricRow, String> pluginName = new TableColumn<>("Plugin");
        pluginName.setCellValueFactory(data -> data.getValue().pluginNameProperty());
        TableColumn<PluginMetricRow, String> metricName = new TableColumn<>("Metric");
        metricName.setCellValueFactory(data -> data.getValue().metricNameProperty());
        TableColumn<PluginMetricRow, String> value = new TableColumn<>("Value");
        value.setCellValueFactory(data -> data.getValue().valueProperty());

        table.getColumns().addAll(pluginName, metricName, value);

        root.getChildren().addAll(title, table);
    }

    public Node node() {
        return root;
    }

    public void bind(ObservableList<PluginMetricRow> entries) {
        table.setItems(entries);
    }
}
