package com.emirhanunsal.mcservercontroller;

import com.emirhanunsal.mcservercontroller.config.AwsProperties;
import com.emirhanunsal.mcservercontroller.config.AppSecurityProperties;
import com.emirhanunsal.mcservercontroller.config.MinecraftProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AwsProperties.class, AppSecurityProperties.class, MinecraftProperties.class})
public class MinecraftServerControllerApplication {
    public static void main(String[] args) { SpringApplication.run(MinecraftServerControllerApplication.class, args); }
}
