package com.servercontroller.app.ui.tabs;

import com.servercontroller.app.net.DiscoveryListener;
import com.servercontroller.app.net.ServerConnection;
import com.servercontroller.app.ui.components.ConnectionForm;
import com.servercontroller.app.ui.tabs.server.ServerTabContent;
import com.servercontroller.app.ui.viewmodel.ServerTabViewModel;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;

public class ServersTabPane {
    private final TabPane tabPane;
    private final DiscoveryListener discoveryListener;

    public ServersTabPane() {
        tabPane = new TabPane();
        tabPane.getTabs().add(buildAddServerTab());
        tabPane.getStyleClass().add("server-tabs");
        discoveryListener = new DiscoveryListener(8766);
        discoveryListener.start(discovery -> Platform.runLater(() -> addDiscoveredTab(discovery.serverName(),
                discovery.host(), discovery.port())));
    }

    public Node node() {
        return tabPane;
    }

    private Tab buildAddServerTab() {
        Tab tab = new Tab("+");
        tab.setClosable(false);
        tab.setTooltip(new Tooltip("Add server"));
        ConnectionForm form = new ConnectionForm();
        form.addButton().setOnAction(event -> addServerTab(form));
        tab.setContent(form.node());
        return tab;
    }

    private void addServerTab(ConnectionForm form) {
        String name = form.nameField().getText().isBlank() ? "Server" : form.nameField().getText();
        String host = form.hostField().getText();
        int port = Integer.parseInt(form.portField().getText());
        String apiKey = form.apiKeyField().getText();
        addConnectedTab(name, host, port, apiKey);
    }

    private void addDiscoveredTab(String name, String host, int port) {
        String displayName = name == null || name.isBlank() ? host : name;
        ConnectionForm form = new ConnectionForm();
        form.nameField().setText(displayName);
        form.hostField().setText(host);
        form.portField().setText(Integer.toString(port));
        form.addButton().setOnAction(event -> addServerTab(form));
        Tab tab = new Tab(displayName + " (discovered)");
        tab.setClosable(true);
        tab.setContent(form.node());
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, tab);
    }

    private void addConnectedTab(String name, String host, int port, String apiKey) {
        String url = "wss://" + host + ":" + port + "/ws";
        ServerConnection connection = new ServerConnection(url, apiKey);
        ServerTabViewModel viewModel = new ServerTabViewModel(connection);
        Tab serverTab = new Tab(name);
        serverTab.setContent(new ServerTabContent(name, viewModel).node());
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, serverTab);
        tabPane.getSelectionModel().select(serverTab);
    }
}
