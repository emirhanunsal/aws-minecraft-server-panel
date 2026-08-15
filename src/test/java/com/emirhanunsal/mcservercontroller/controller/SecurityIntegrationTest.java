package com.emirhanunsal.mcservercontroller.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.security.username=test-user", "app.security.password=test-password"})
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    @Autowired MockMvc mvc;

    @Test void anonymousUserIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test void configuredCredentialsCanLogIn() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "test-user").param("password", "test-password"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }

    @Test void authenticatedUserCanOpenControlPanel() throws Exception {
        mvc.perform(get("/").with(user("test-user"))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Minecraft Server Controller")));
    }

    @Test void stateChangingRequestRequiresCsrf() throws Exception {
        mvc.perform(post("/api/start").with(user("test-user"))).andExpect(status().isForbidden());
    }

    @Test void healthRemainsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
