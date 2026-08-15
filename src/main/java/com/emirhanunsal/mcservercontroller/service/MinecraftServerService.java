package com.emirhanunsal.mcservercontroller.service;

import com.emirhanunsal.mcservercontroller.client.LambdaMinecraftClient;
import com.emirhanunsal.mcservercontroller.config.MinecraftProperties;
import com.emirhanunsal.mcservercontroller.dto.LambdaResult;
import com.emirhanunsal.mcservercontroller.dto.ServerResponse;
import com.emirhanunsal.mcservercontroller.dto.ServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MinecraftServerService {
    private static final Logger log = LoggerFactory.getLogger(MinecraftServerService.class);
    private final LambdaMinecraftClient client;
    private final MinecraftProperties minecraftProperties;

    public MinecraftServerService(LambdaMinecraftClient client, MinecraftProperties minecraftProperties) {
        this.client = client;
        this.minecraftProperties = minecraftProperties;
    }

    public ServerResponse status() { return execute("status"); }
    public ServerResponse start() { return execute("start"); }
    public ServerResponse stop() { return execute("stop"); }

    private ServerResponse execute(String action) {
        log.info("event=server_action_requested action={}", action);
        LambdaResult result = client.invoke(action);
        String message = result.message() != null ? result.message() : defaultMessage(action, result.state());
        String address = result.minecraftAddress() != null ? result.minecraftAddress() : minecraftProperties.address();
        log.info("event=server_action_result action={} state={}", action, result.state());
        return ServerResponse.success(result.state(), message, address);
    }

    private String defaultMessage(String action, ServerState state) {
        if ("start".equals(action) && state == ServerState.RUNNING) return "Minecraft server is already running";
        if ("stop".equals(action) && state == ServerState.STOPPED) return "Minecraft server is already stopped";
        return switch (state) {
            case RUNNING -> "Minecraft server is running";
            case STOPPED -> "Minecraft server is stopped";
            case STARTING -> "Minecraft server is starting";
            case STOPPING -> "Minecraft server is stopping";
            case UNKNOWN -> "Minecraft server state is unknown";
            case ERROR -> "Minecraft server status is unavailable";
        };
    }
}
