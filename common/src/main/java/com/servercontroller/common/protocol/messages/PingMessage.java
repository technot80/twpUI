package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record PingMessage(long timestamp) implements Message {
}
