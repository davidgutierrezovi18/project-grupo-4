package es.nextjourney.vs_nextjourney;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityClassTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousUserRedirectedFromProtectedPage() throws Exception {
        mockMvc.perform(get("/mytravels"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/sign_in")));
    }

    @Test
    void anonymousUserCanAccessPublicPage() throws Exception {
        mockMvc.perform(get("/destinations"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserCanAccessPublicApiReadEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/destinations"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserCannotAccessProtectedApiEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserCannotUseAdminOnlyApiWriteEndpoint() throws Exception {
        mockMvc.perform(put("/api/v1/destinations/1")
                        .with(user("ana").roles("USER"))
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"Destino\",\"country\":\"España\",\"description\":\"Desc\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReachAdminOnlyApiWriteEndpoint() throws Exception {
        mockMvc.perform(put("/api/v1/destinations/1")
                        .with(user("root").roles("ADMIN"))
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"Destino\",\"country\":\"España\",\"description\":\"Desc\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCanAccessProtectedPage() throws Exception {
        mockMvc.perform(get("/mytravels").with(user("ana").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void nonAdminCannotAccessAdminRoutes() throws Exception {
        mockMvc.perform(get("/admin/test").with(user("ana").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRoutePassesAuthorizationForAdminUser() throws Exception {
        mockMvc.perform(get("/admin/test").with(user("root").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
}
