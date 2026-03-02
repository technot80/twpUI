package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record DownloadUpdateMessage(String pluginName, String downloadUrl) implements Message {
}
