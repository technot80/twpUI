package com.servercontroller.common.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Protocol {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Protocol() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static String encode(Message message) throws JsonProcessingException {
        return MAPPER.writeValueAsString(message);
    }

    public static Message decode(String payload) throws JsonProcessingException {
        return MAPPER.readValue(payload, Message.class);
    }
}
