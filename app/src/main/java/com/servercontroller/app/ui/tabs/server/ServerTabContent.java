package com.servercontroller.app.ui.tabs.server;

import com.servercontroller.app.ui.components.MetricsPanel;
import com.servercontroller.app.ui.components.PluginMetricsPanel;
import com.servercontroller.app.ui.components.PluginPanel;
import com.servercontroller.app.ui.components.ServerControlPanel;
import com.servercontroller.app.ui.components.ServerLogPanel;
import com.servercontroller.app.ui.viewmodel.ServerTabViewModel;
import com.servercontroller.common.protocol.messages.LogMessage;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ServerTabContent {
    private final BorderPane root;
    private Label status;

    public ServerTabContent(String serverName, ServerTabViewModel viewModel) {
        root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setTop(buildHeader(serverName));
        root.setCenter(buildContent(viewModel));
        root.getStyleClass().add("server-tab");

        status.textProperty().bind(viewModel.statusProperty());
        viewModel.connect();
    }

    public Node node() {
        return root;
    }

    private HBox buildHeader(String serverName) {
        HBox header = new HBox();
        header.getStyleClass().add("server-header");
        Label name = new Label(serverName);
        name.getStyleClass().add("server-name");
        status = new Label("Offline");
        status.getStyleClass().add("server-status");
        header.getChildren().addAll(name, status);
        HBox.setHgrow(status, Priority.ALWAYS);
        return header;
    }

    private VBox buildContent(ServerTabViewModel viewModel) {
        VBox container = new VBox(16);
        MetricsPanel metricsPanel = new MetricsPanel();
        metricsPanel.bind(viewModel);

        PluginMetricsPanel pluginMetricsPanel = new PluginMetricsPanel();
        pluginMetricsPanel.bind(viewModel.pluginMetrics());

        PluginPanel pluginPanel = new PluginPanel();
        pluginPanel.table().setItems(viewModel.plugins());
        pluginPanel.checkUpdatesButton().setOnAction(event -> viewModel.checkUpdates());
        pluginPanel.setOnUpdate(viewModel::downloadUpdate);
        viewModel.setUpdateInfoListener((name, info) -> pluginPanel.updateInfo(name, info.hasUpdate(), info.downloadUrl()));

        ServerLogPanel logPanel = new ServerLogPanel();
        viewModel.logs().addListener((javafx.collections.ListChangeListener<? super LogMessage>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (LogMessage log : change.getAddedSubList()) {
                        logPanel.appendLog("[" + log.level() + "] " + log.message());
                    }
                }
            }
        });
        logPanel.errorToggle().setOnAction(event -> viewModel.setLogLevels(logPanel.selectedLogLevels()));
        logPanel.warnToggle().setOnAction(event -> viewModel.setLogLevels(logPanel.selectedLogLevels()));
        logPanel.infoToggle().setOnAction(event -> viewModel.setLogLevels(logPanel.selectedLogLevels()));
        logPanel.debugToggle().setOnAction(event -> viewModel.setLogLevels(logPanel.selectedLogLevels()));

        ServerControlPanel controlPanel = new ServerControlPanel();
        controlPanel.sendButton().setOnAction(event -> viewModel.broadcast(controlPanel.messageField().getText()));
        controlPanel.restartButton().setOnAction(event -> viewModel.restart());
        controlPanel.stopButton().setOnAction(event -> viewModel.stop());

        container.getChildren().addAll(
                metricsPanel.node(),
                pluginMetricsPanel.node(),
                pluginPanel.node(),
                logPanel.node(),
                controlPanel.node()
        );
        VBox.setVgrow(logPanel.node(), Priority.ALWAYS);
        return container;
    }
}
