package com.servercontroller.common.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.servercontroller.common.protocol.messages.AuthMessage;
import com.servercontroller.common.protocol.messages.AuthResultMessage;
import com.servercontroller.common.protocol.messages.BroadcastMessage;
import com.servercontroller.common.protocol.messages.CheckUpdatesMessage;
import com.servercontroller.common.protocol.messages.ControlMessage;
import com.servercontroller.common.protocol.messages.DiscoveryMessage;
import com.servercontroller.common.protocol.messages.DownloadUpdateMessage;
import com.servercontroller.common.protocol.messages.ErrorMessage;
import com.servercontroller.common.protocol.messages.GetPluginsMessage;
import com.servercontroller.common.protocol.messages.LogMessage;
import com.servercontroller.common.protocol.messages.MetricsMessage;
import com.servercontroller.common.protocol.messages.PingMessage;
import com.servercontroller.common.protocol.messages.PluginListMessage;
import com.servercontroller.common.protocol.messages.PongMessage;
import com.servercontroller.common.protocol.messages.SubscribeMessage;
import com.servercontroller.common.protocol.messages.UpdateDownloadedMessage;
import com.servercontroller.common.protocol.messages.UpdateInfoMessage;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AuthMessage.class, name = "AUTH"),
        @JsonSubTypes.Type(value = AuthResultMessage.class, name = "AUTH_RESULT"),
        @JsonSubTypes.Type(value = SubscribeMessage.class, name = "SUBSCRIBE"),
        @JsonSubTypes.Type(value = LogMessage.class, name = "LOG"),
        @JsonSubTypes.Type(value = MetricsMessage.class, name = "METRICS"),
        @JsonSubTypes.Type(value = GetPluginsMessage.class, name = "GET_PLUGINS"),
        @JsonSubTypes.Type(value = PluginListMessage.class, name = "PLUGIN_LIST"),
        @JsonSubTypes.Type(value = CheckUpdatesMessage.class, name = "CHECK_UPDATES"),
        @JsonSubTypes.Type(value = UpdateInfoMessage.class, name = "UPDATE_INFO"),
        @JsonSubTypes.Type(value = DownloadUpdateMessage.class, name = "DOWNLOAD_UPDATE"),
        @JsonSubTypes.Type(value = UpdateDownloadedMessage.class, name = "UPDATE_DOWNLOADED"),
        @JsonSubTypes.Type(value = BroadcastMessage.class, name = "BROADCAST"),
        @JsonSubTypes.Type(value = ControlMessage.class, name = "CONTROL"),
        @JsonSubTypes.Type(value = PingMessage.class, name = "PING"),
        @JsonSubTypes.Type(value = PongMessage.class, name = "PONG"),
        @JsonSubTypes.Type(value = ErrorMessage.class, name = "ERROR"),
        @JsonSubTypes.Type(value = DiscoveryMessage.class, name = "DISCOVERY")
})
public interface Message {
}
