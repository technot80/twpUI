package com.servercontroller.plugin.control;

public enum ControlAction {
    STOP,
    RESTART;

    public static ControlAction fromString(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.trim().toLowerCase()) {
            case "stop" -> STOP;
            case "restart" -> RESTART;
            default -> null;
        };
    }

    public String wireValue() {
        return name().toLowerCase();
    }
}
