package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record LogMessage(String level, String message, long timestamp) implements Message {
}
