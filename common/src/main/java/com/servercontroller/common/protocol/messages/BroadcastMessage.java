package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record BroadcastMessage(String message) implements Message {
}
