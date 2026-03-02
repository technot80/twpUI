package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record ControlMessage(String action) implements Message {
}
