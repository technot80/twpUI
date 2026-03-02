package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record AuthMessage(String apiKey, String clientVersion) implements Message {
}
