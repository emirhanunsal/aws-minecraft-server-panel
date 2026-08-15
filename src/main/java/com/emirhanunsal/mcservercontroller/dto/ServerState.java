package com.emirhanunsal.mcservercontroller.dto;

import java.util.Locale;

public enum ServerState {
    RUNNING, STOPPED, STARTING, STOPPING, UNKNOWN, ERROR;

    public static ServerState fromAwsState(String state) {
        if (state == null) return UNKNOWN;
        return switch (state.toLowerCase(Locale.ROOT)) {
            case "running" -> RUNNING;
            case "stopped" -> STOPPED;
            case "pending" -> STARTING;
            case "stopping", "shutting-down" -> STOPPING;
            default -> UNKNOWN;
        };
    }
}
