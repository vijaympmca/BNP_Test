package com.bookstore.support;

import com.bookstore.application.BookstoreApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for MockMvc integration tests: full Spring context, in-memory H2, real security.
 * Every test creates its own users (unique names), so tests don't depend on each other.
 */
@SpringBootTest(classes = BookstoreApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ApiTestSupport {

    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "Bookstore@2026";
    protected static final String VALID_PASSWORD = "Secure#Pass1";

    private static final AtomicInteger COUNTER = new AtomicInteger();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    protected JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected String uniqueUsername() {
        return "user" + System.nanoTime() % 100000 + "x" + COUNTER.incrementAndGet();
    }

    protected Map<String, Object> registerBody(String username) {
        return Map.of(
                "fullName", "Test User",
                "username", username,
                "email", username + "@example.com",
                "password", VALID_PASSWORD,
                "confirmPassword", VALID_PASSWORD);
    }

    /** Registers a new USER and returns "Bearer <token>". */
    protected String registerAndLogin() throws Exception {
        String username = uniqueUsername();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerBody(username))))
                .andExpect(status().isCreated());
        return login(username, VALID_PASSWORD);
    }

    protected String adminToken() throws Exception {
        return login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    protected String login(String usernameOrEmail, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usernameOrEmail", usernameOrEmail, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + body(result).path("data").path("accessToken").asText();
    }

    /** Creates a book with its own stock (via the ADMIN API) and returns its id. */
    protected long createBook(int stock, String price) throws Exception {
        String isbn = "978" + String.format("%010d", Math.abs(System.nanoTime() % 10_000_000_000L));
        MvcResult result = mockMvc.perform(post("/api/books")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Test Book " + isbn,
                                "author", "Test Author",
                                "isbn", isbn,
                                "price", price,
                                "stock", stock))))
                .andExpect(status().isCreated())
                .andReturn();
        return body(result).path("data").path("id").asLong();
    }
}
