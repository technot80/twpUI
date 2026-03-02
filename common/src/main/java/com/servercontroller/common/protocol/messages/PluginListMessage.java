package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;
import java.util.List;

public record PluginListMessage(List<PluginSummary> plugins) implements Message {
    public record PluginSummary(String name, String version, String author, boolean enabled) {
    }
}
