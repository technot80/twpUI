package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;
import java.util.List;

public record CheckUpdatesMessage(List<String> plugins) implements Message {
}
