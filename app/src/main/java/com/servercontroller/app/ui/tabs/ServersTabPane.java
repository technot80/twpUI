package com.servercontroller.app.ui.tabs;

import com.servercontroller.app.config.AppStorage;
import com.servercontroller.app.config.KeychainService;
import com.servercontroller.app.config.ProfileStore;
import com.servercontroller.app.config.ServerProfile;
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
    private final ProfileStore profileStore;
    private final KeychainService keychainService;
    private final java.util.Map<String, Tab> discoveredTabs;
    private final java.util.Map<String, Tab> savedTabs;

    public ServersTabPane() {
        tabPane = new TabPane();
        profileStore = new ProfileStore();
        keychainService = new KeychainService();
        discoveredTabs = new java.util.HashMap<>();
        savedTabs = new java.util.HashMap<>();
        tabPane.getTabs().add(buildAddServerTab());
        tabPane.getStyleClass().add("server-tabs");
        discoveryListener = new DiscoveryListener(8766);
        loadSavedServers();
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
        String id = buildId(host, port);
        saveProfile(new ServerProfile(id, name, host, port), apiKey);
        addConnectedTab(name, host, port, apiKey);
    }

    private void addDiscoveredTab(String name, String host, int port) {
        String id = buildId(host, port);
        if (savedTabs.containsKey(id) || discoveredTabs.containsKey(id)) {
            return;
        }
        String displayName = name == null || name.isBlank() ? host : name;
        ConnectionForm form = new ConnectionForm();
        form.nameField().setText(displayName);
        form.hostField().setText(host);
        form.portField().setText(Integer.toString(port));
        form.addButton().setOnAction(event -> addServerTab(form));
        Tab tab = new Tab(displayName + " (discovered)");
        tab.setClosable(true);
        tab.setContent(form.node());
        discoveredTabs.put(id, tab);
        tab.setOnClosed(event -> discoveredTabs.remove(id));
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, tab);
    }

    private void addConnectedTab(String name, String host, int port, String apiKey) {
        String id = buildId(host, port);
        if (savedTabs.containsKey(id)) {
            tabPane.getSelectionModel().select(savedTabs.get(id));
            return;
        }
        String url = "wss://" + host + ":" + port + "/ws";
        ServerConnection connection = new ServerConnection(url, apiKey);
        ServerTabViewModel viewModel = new ServerTabViewModel(connection);
        Tab serverTab = new Tab(name);
        serverTab.setContent(new ServerTabContent(name, viewModel).node());
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, serverTab);
        tabPane.getSelectionModel().select(serverTab);
        savedTabs.put(id, serverTab);
        Tab discovered = discoveredTabs.remove(id);
        if (discovered != null) {
            tabPane.getTabs().remove(discovered);
        }
    }

    private void loadSavedServers() {
        java.util.List<ServerProfile> profiles = profileStore.load(AppStorage.baseDir());
        for (ServerProfile profile : profiles) {
            String apiKey = keychainService.loadApiKey(profile.id());
            addConnectedTab(profile.name(), profile.host(), profile.port(), apiKey == null ? "" : apiKey);
        }
    }

    private void saveProfile(ServerProfile profile, String apiKey) {
        java.util.List<ServerProfile> profiles = new java.util.ArrayList<>(profileStore.load(AppStorage.baseDir()));
        profiles.removeIf(existing -> existing.id().equals(profile.id()));
        profiles.add(profile);
        profileStore.save(AppStorage.baseDir(), profiles);
        if (apiKey != null && !apiKey.isBlank()) {
            keychainService.saveApiKey(profile.id(), apiKey);
        }
    }

    private String buildId(String host, int port) {
        return host + ":" + port;
    }
}
