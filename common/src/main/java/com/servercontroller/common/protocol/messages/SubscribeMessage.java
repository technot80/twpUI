package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

import java.util.List;

public record SubscribeMessage(List<String> filters, List<String> logLevels) implements Message {
}
