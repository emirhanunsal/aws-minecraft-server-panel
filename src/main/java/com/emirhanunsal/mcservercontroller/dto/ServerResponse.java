package com.emirhanunsal.mcservercontroller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServerResponse(boolean success, ServerState state, String message, String minecraftAddress) {
    public static ServerResponse success(ServerState state, String message, String address) {
        return new ServerResponse(true, state, message, address);
    }
}
