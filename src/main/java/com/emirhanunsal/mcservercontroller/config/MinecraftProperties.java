package com.emirhanunsal.mcservercontroller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.minecraft")
public record MinecraftProperties(String address) {}
