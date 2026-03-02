package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record UpdateDownloadedMessage(String pluginName, String fileName, boolean requiresRestart) implements Message {
}
