package com.bookstore.controller;

import com.bookstore.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookControllerTest extends ApiTestSupport {

    @Test
    void listBooks_isPublic_andPaged() throws Exception {
        mockMvc.perform(get("/api/books?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(5))
                .andExpect(jsonPath("$.data.size").value(5));
    }

    @Test
    void listBooks_search_filtersByTitleOrAuthor() throws Exception {
        mockMvc.perform(get("/api/books?search=bloch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].author").value("Joshua Bloch"));
    }

    @Test
    void listBooks_badPaging_returns400() throws Exception {
        mockMvc.perform(get("/api/books?page=-1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/books?size=1000")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/books?sortBy=password")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/books?page=abc")).andExpect(status().isBadRequest());
    }

    @Test
    void getBook_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/books/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with id: 999999"));
    }

    @Test
    void createBook_asUser_returns403() throws Exception {
        mockMvc.perform(post("/api/books").header("Authorization", registerAndLogin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "X", "author", "Y", "isbn", "9780000000001",
                                "price", "10.00", "stock", 1))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBook_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createBook_asAdmin_invalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/books").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "", "isbn", "123", "price", "-1", "stock", -5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.isbn").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists())
                .andExpect(jsonPath("$.validationErrors.stock").exists());
    }

    @Test
    void createAndDeleteBook_asAdmin_returns201Then204() throws Exception {
        long id = createBook(3, "12.50");
        mockMvc.perform(get("/api/books/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(12.50));
        mockMvc.perform(delete("/api/books/" + id).header("Authorization", adminToken()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/books/" + id)).andExpect(status().isNotFound());
    }
}
