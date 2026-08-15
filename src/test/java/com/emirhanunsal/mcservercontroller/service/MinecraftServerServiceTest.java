package com.emirhanunsal.mcservercontroller.service;

import com.emirhanunsal.mcservercontroller.client.LambdaMinecraftClient;
import com.emirhanunsal.mcservercontroller.config.MinecraftProperties;
import com.emirhanunsal.mcservercontroller.dto.LambdaResult;
import com.emirhanunsal.mcservercontroller.dto.ServerState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MinecraftServerServiceTest {
    private final LambdaMinecraftClient client = mock(LambdaMinecraftClient.class);
    private final MinecraftServerService service = new MinecraftServerService(client, new MinecraftProperties("minecraft.test"));

    @Test void startAndAlreadyRunningAreSuccessful() {
        when(client.invoke("start")).thenReturn(new LambdaResult(ServerState.STARTING, null, null));
        assertEquals(ServerState.STARTING, service.start().state());
        when(client.invoke("start")).thenReturn(new LambdaResult(ServerState.RUNNING, null, null));
        assertTrue(service.start().message().contains("already running"));
    }

    @Test void stopAndAlreadyStoppedAreSuccessful() {
        when(client.invoke("stop")).thenReturn(new LambdaResult(ServerState.STOPPING, null, null));
        assertEquals(ServerState.STOPPING, service.stop().state());
        when(client.invoke("stop")).thenReturn(new LambdaResult(ServerState.STOPPED, null, null));
        assertTrue(service.stop().message().contains("already stopped"));
    }

    @Test void statusUsesDefaultAddress() {
        when(client.invoke("status")).thenReturn(new LambdaResult(ServerState.RUNNING, null, null));
        assertEquals("minecraft.test", service.status().minecraftAddress());
    }
}
