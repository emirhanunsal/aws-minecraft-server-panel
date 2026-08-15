package com.emirhanunsal.mcservercontroller.controller;

import com.emirhanunsal.mcservercontroller.dto.ServerResponse;
import com.emirhanunsal.mcservercontroller.dto.ServerState;
import com.emirhanunsal.mcservercontroller.service.MinecraftServerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MinecraftApiController {
    private final MinecraftServerService service;
    public MinecraftApiController(MinecraftServerService service) { this.service = service; }

    @GetMapping("/status") public ServerResponse status() { return service.status(); }
    @PostMapping("/start") public ResponseEntity<ServerResponse> start() { return operationResponse(service.start()); }
    @PostMapping("/stop") public ResponseEntity<ServerResponse> stop() { return operationResponse(service.stop()); }

    private ResponseEntity<ServerResponse> operationResponse(ServerResponse response) {
        boolean transitional = response.state() == ServerState.STARTING || response.state() == ServerState.STOPPING;
        return ResponseEntity.status(transitional ? 202 : 200).body(response);
    }
}
