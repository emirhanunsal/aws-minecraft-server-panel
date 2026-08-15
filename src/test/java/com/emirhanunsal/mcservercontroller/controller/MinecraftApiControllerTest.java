package com.emirhanunsal.mcservercontroller.controller;

import com.emirhanunsal.mcservercontroller.dto.ServerResponse;
import com.emirhanunsal.mcservercontroller.dto.ServerState;
import com.emirhanunsal.mcservercontroller.exception.ApiExceptionHandler;
import com.emirhanunsal.mcservercontroller.exception.LambdaInvocationException;
import com.emirhanunsal.mcservercontroller.service.MinecraftServerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MinecraftApiControllerTest {
    private MinecraftServerService service;
    private MockMvc mvc;
    @BeforeEach void setup() { service=mock(MinecraftServerService.class); mvc=MockMvcBuilders.standaloneSetup(new MinecraftApiController(service)).setControllerAdvice(new ApiExceptionHandler()).build(); }

    @Test void statusReturns200() throws Exception {
        when(service.status()).thenReturn(ServerResponse.success(ServerState.RUNNING,"running","mc.example.com"));
        mvc.perform(get("/api/status")).andExpect(status().isOk()).andExpect(jsonPath("$.state").value("RUNNING"));
    }
    @Test void transitionsReturn202AndIdempotentResultsReturn200() throws Exception {
        when(service.start()).thenReturn(ServerResponse.success(ServerState.STARTING,"starting",null));
        mvc.perform(post("/api/start")).andExpect(status().isAccepted());
        when(service.stop()).thenReturn(ServerResponse.success(ServerState.STOPPED,"already stopped",null));
        mvc.perform(post("/api/stop")).andExpect(status().isOk());
    }
    @Test void lambdaFailureIsSanitized() throws Exception {
        when(service.status()).thenThrow(new LambdaInvocationException("secret AWS detail"));
        mvc.perform(get("/api/status")).andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.success").value(false)).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));
    }
}
