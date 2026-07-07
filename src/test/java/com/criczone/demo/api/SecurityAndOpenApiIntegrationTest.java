package com.criczone.demo.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.jwt.secret=not-a-base64-secret-with-32-bytes!!")
@AutoConfigureMockMvc
class SecurityAndOpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedBookingEndpointRejectsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/bookings"))
            .andExpect(status().isForbidden());
    }

    @Test
    void openApiDocsArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("CricZone API")));
    }
}
