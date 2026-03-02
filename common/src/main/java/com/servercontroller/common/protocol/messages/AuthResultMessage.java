package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record AuthResultMessage(boolean success, String message, String serverVersion, String serverType) implements Message {
}
