package com.servercontroller.app.ui.components;

import com.servercontroller.common.protocol.messages.PluginListMessage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PluginPanel {
    private final VBox root;
    private final TableView<PluginListMessage.PluginSummary> table;
    private final ObservableList<PluginListMessage.PluginSummary> plugins;
    private final Button checkUpdates;
    private final Map<String, UpdateInfo> updateInfoMap;
    private BiConsumer<String, String> onUpdate;

    public PluginPanel() {
        root = new VBox(12);
        root.getStyleClass().add("panel");
        root.setPadding(new Insets(12));

        HBox header = new HBox(12);
        Label title = new Label("Plugins");
        title.getStyleClass().add("panel-title");
        checkUpdates = new Button("Check updates");
        checkUpdates.getStyleClass().add("primary-button");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, checkUpdates);

        table = new TableView<>();
        plugins = FXCollections.observableArrayList();
        updateInfoMap = new HashMap<>();
        table.setItems(plugins);
        TableColumn<PluginListMessage.PluginSummary, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().name()));
        TableColumn<PluginListMessage.PluginSummary, String> version = new TableColumn<>("Version");
        version.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().version()));
        TableColumn<PluginListMessage.PluginSummary, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().enabled() ? "Enabled" : "Disabled"));
        TableColumn<PluginListMessage.PluginSummary, String> update = new TableColumn<>("Update");
        update.setCellFactory(column -> new TableCell<>() {
            private final Button button = new Button("Update");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= table.getItems().size()) {
                    setGraphic(null);
                    return;
                }
                PluginListMessage.PluginSummary plugin = table.getItems().get(getIndex());
                UpdateInfo info = updateInfoMap.get(plugin.name());
                if (info == null || !info.hasUpdate()) {
                    setGraphic(new Label("Up to date"));
                } else {
                    button.setOnAction(event -> {
                        if (onUpdate != null) {
                            onUpdate.accept(plugin.name(), info.downloadUrl());
                        }
                    });
                    setGraphic(button);
                }
            }
        });

        table.getColumns().addAll(name, version, status, update);

        root.getChildren().addAll(header, table);
    }

    public Node node() {
        return root;
    }

    public void updatePlugins(java.util.List<PluginListMessage.PluginSummary> list) {
        plugins.setAll(list);
    }

    public void updateInfo(String pluginName, boolean hasUpdate, String downloadUrl) {
        updateInfoMap.put(pluginName, new UpdateInfo(hasUpdate, downloadUrl));
        table.refresh();
    }

    public Button checkUpdatesButton() {
        return checkUpdates;
    }

    public TableView<PluginListMessage.PluginSummary> table() {
        return table;
    }

    public void setOnUpdate(BiConsumer<String, String> onUpdate) {
        this.onUpdate = onUpdate;
    }

    private record UpdateInfo(boolean hasUpdate, String downloadUrl) {
    }
}
