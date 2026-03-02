package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record ErrorMessage(String code, String message) implements Message {
}
