package com.bookstore.controller;

import com.bookstore.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends ApiTestSupport {

    @Test
    void register_validRequest_returns201AndUserRole() throws Exception {
        String username = uniqueUsername();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(username))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void register_blankFields_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.username").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void register_weakPassword_returns400() throws Exception {
        Map<String, Object> body = new HashMap<>(registerBody(uniqueUsername()));
        body.put("password", "weakpass");
        body.put("confirmPassword", "weakpass");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void register_mismatchedConfirmPassword_returns400() throws Exception {
        Map<String, Object> body = new HashMap<>(registerBody(uniqueUsername()));
        body.put("confirmPassword", "Different#Pass1");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.confirmPassword")
                        .value("Password and confirm password do not match"));
    }

    @Test
    void register_passwordContainsUsername_returns400() throws Exception {
        String username = uniqueUsername();
        Map<String, Object> body = new HashMap<>(registerBody(username));
        String password = "A1#" + username;
        body.put("password", password);
        body.put("confirmPassword", password);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").value("Password must not contain the username"));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        String username = uniqueUsername();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(username))))
                .andExpect(status().isCreated());

        Map<String, Object> again = new HashMap<>(registerBody(username.toUpperCase()));
        again.put("email", "other." + username + "@example.com");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json(again)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_withUsernameOrEmail_returnsToken() throws Exception {
        String username = uniqueUsername();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(username))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usernameOrEmail", username + "@example.com", "password", VALID_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void login_wrongPassword_returns401WithGenericMessage() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usernameOrEmail", "admin", "password", "Wrong#Pass1"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"));
    }

    @Test
    void login_seededAdmin_returnsAdministratorRole() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usernameOrEmail", ADMIN_USERNAME, "password", ADMIN_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.roleName").value("Administrator"));
    }

    @Test
    void me_withoutToken_returns401_andWithToken_returns200() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me").header("Authorization", registerAndLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }
}
