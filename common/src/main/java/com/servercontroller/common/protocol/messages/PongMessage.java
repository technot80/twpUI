package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record PongMessage(long timestamp) implements Message {
}
