package com.servercontroller.common.protocol.messages;

import com.servercontroller.common.protocol.Message;

public record UpdateInfoMessage(String pluginName, boolean hasUpdate, String currentVersion,
                                String latestVersion, String downloadUrl, String provider) implements Message {
}
