package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record DiscoveryMessage(String serverName, String host, int port, String serverType) implements Message {
}
