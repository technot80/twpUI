package com.servercontroller.common.protocol;

public enum MessageType {
    AUTH,
    AUTH_RESULT,
    SUBSCRIBE,
    LOG,
    METRICS,
    GET_PLUGINS,
    PLUGIN_LIST,
    CHECK_UPDATES,
    UPDATE_INFO,
    DOWNLOAD_UPDATE,
    UPDATE_DOWNLOADED,
    BROADCAST,
    CONTROL,
    PING,
    PONG,
    ERROR,
    DISCOVERY
}
