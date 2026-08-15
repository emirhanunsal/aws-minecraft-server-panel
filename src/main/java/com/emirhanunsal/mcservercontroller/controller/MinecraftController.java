package com.emirhanunsal.mcservercontroller.controller;

import com.emirhanunsal.mcservercontroller.config.MinecraftProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MinecraftController {
    private final MinecraftProperties minecraftProperties;

    public MinecraftController(MinecraftProperties minecraftProperties) { this.minecraftProperties = minecraftProperties; }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("minecraftAddress", minecraftProperties.address());
        return "index";
    }

    @GetMapping("/login")
    public String login() { return "login"; }
}
