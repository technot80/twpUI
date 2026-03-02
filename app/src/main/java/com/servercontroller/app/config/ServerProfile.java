package com.servercontroller.app.config;

public record ServerProfile(String id, String name, String host, int port) {
    public String url() {
        return "wss://" + host + ":" + port + "/ws";
    }
}
