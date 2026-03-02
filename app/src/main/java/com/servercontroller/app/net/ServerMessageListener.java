package com.servercontroller.app.net;

import com.servercontroller.common.protocol.Message;

public interface ServerMessageListener {
    void onMessage(Message message);

    void onError(Throwable error);
}
