package com.servercontroller.app.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PluginMetricRow {
    private final StringProperty pluginName = new SimpleStringProperty("");
    private final StringProperty metricName = new SimpleStringProperty("");
    private final StringProperty value = new SimpleStringProperty("");

    public PluginMetricRow(String pluginName, String metricName, String value) {
        this.pluginName.set(pluginName);
        this.metricName.set(metricName);
        this.value.set(value);
    }

    public StringProperty pluginNameProperty() {
        return pluginName;
    }

    public StringProperty metricNameProperty() {
        return metricName;
    }

    public StringProperty valueProperty() {
        return value;
    }
}
