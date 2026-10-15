package com.bookstore.controller;

import com.bookstore.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerTest extends ApiTestSupport {

    @Test
    void adminEndpoints_asUser_return403() throws Exception {
        String user = registerAndLogin();
        mockMvc.perform(get("/api/admin/users").header("Authorization", user)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/orders").header("Authorization", user)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/reports/top-books").header("Authorization", user))
                .andExpect(status().isForbidden());
    }

    @Test
    void reports_asAdmin_return200() throws Exception {
        String user = registerAndLogin();
        long book = createBook(5, "10.00");
        mockMvc.perform(post("/api/cart/items").header("Authorization", user).contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("bookId", book, "quantity", 2)))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/orders").header("Authorization", user)).andExpect(status().isCreated());

        String admin = adminToken();
        mockMvc.perform(get("/api/admin/reports/top-books?limit=50").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].totalSold").exists());
        mockMvc.perform(get("/api/admin/reports/order-summary").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").exists());
        mockMvc.perform(get("/api/admin/reports/top-books?limit=0").header("Authorization", admin))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/admin/orders").header("Authorization", admin))
                .andExpect(status().isOk());
    }

    @Test
    void promoteUserToAdmin_thenUserCanUseAdminEndpoints() throws Exception {
        String username = uniqueUsername();
        MvcResult registered = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(username))))
                .andExpect(status().isCreated()).andReturn();
        long userId = body(registered).path("data").path("id").asLong();
        String userToken = login(username, VALID_PASSWORD);

        mockMvc.perform(get("/api/admin/users").header("Authorization", userToken)).andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/admin/users/" + userId + "/role").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleName").value("Administrator"));

        // same token now works, because the role is re-read from the database on every request
        mockMvc.perform(get("/api/admin/users").header("Authorization", userToken)).andExpect(status().isOk());
    }

    @Test
    void invalidRole_returns400() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/role").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"SUPERUSER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disableUser_blocksLoginWith403_andAdminCannotDisableSelf() throws Exception {
        String username = uniqueUsername();
        MvcResult registered = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(username))))
                .andExpect(status().isCreated()).andReturn();
        long userId = body(registered).path("data").path("id").asLong();

        String admin = adminToken();
        mockMvc.perform(patch("/api/admin/users/" + userId + "/status").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("enabled", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usernameOrEmail", username, "password", VALID_PASSWORD))))
                .andExpect(status().isForbidden());

        MvcResult me = mockMvc.perform(get("/api/auth/me").header("Authorization", admin)).andReturn();
        long adminId = body(me).path("data").path("id").asLong();
        mockMvc.perform(patch("/api/admin/users/" + adminId + "/status").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("enabled", false))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void health_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
