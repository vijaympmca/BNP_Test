package com.bookstore.controller;

import com.bookstore.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartAndOrderControllerTest extends ApiTestSupport {

    @Test
    void cart_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
    }

    @Test
    void fullFlow_addUpdateRemoveCheckoutCancel() throws Exception {
        String token = registerAndLogin();
        long bookA = createBook(10, "20.00");
        long bookB = createBook(5, "7.25");

        // add A twice → quantities are merged
        addItem(token, bookA, 2).andExpect(status().isCreated());
        addItem(token, bookA, 1).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items[0].quantity").value(3));

        // add B
        addItem(token, bookB, 2).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.distinctItems").value(2))
                .andExpect(jsonPath("$.data.grandTotal").value(74.50));   // 3*20 + 2*7.25

        // update B quantity
        mockMvc.perform(put("/api/cart/items/" + bookB).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("quantity", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grandTotal").value(89.00));

        // database-calculated summary matches
        mockMvc.perform(get("/api/cart/summary").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemCount").value(2))
                .andExpect(jsonPath("$.data.totalQuantity").value(7));

        // remove B
        mockMvc.perform(delete("/api/cart/items/" + bookB).header("Authorization", token))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/cart/items/" + bookB).header("Authorization", token))
                .andExpect(status().isNotFound());

        // checkout → 201, cart emptied, stock reduced
        MvcResult order = mockMvc.perform(post("/api/orders").header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.status").value("PLACED"))
                .andExpect(jsonPath("$.data.totalAmount").value(60.00))
                .andReturn();
        long orderId = body(order).path("data").path("id").asLong();

        mockMvc.perform(get("/api/cart").header("Authorization", token))
                .andExpect(jsonPath("$.data.items.length()").value(0));
        mockMvc.perform(get("/api/books/" + bookA)).andExpect(jsonPath("$.data.stock").value(7));

        // order history
        mockMvc.perform(get("/api/orders").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // another USER cannot see or cancel it → 403
        String other = registerAndLogin();
        mockMvc.perform(get("/api/orders/" + orderId).header("Authorization", other))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/orders/" + orderId + "/cancel").header("Authorization", other))
                .andExpect(status().isForbidden());

        // ADMIN can see it
        mockMvc.perform(get("/api/orders/" + orderId).header("Authorization", adminToken()))
                .andExpect(status().isOk());

        // owner cancels → stock returned; cancelling twice → 409
        mockMvc.perform(post("/api/orders/" + orderId + "/cancel").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        mockMvc.perform(get("/api/books/" + bookA)).andExpect(jsonPath("$.data.stock").value(10));
        mockMvc.perform(post("/api/orders/" + orderId + "/cancel").header("Authorization", token))
                .andExpect(status().isConflict());
    }

    @Test
    void addItem_moreThanStock_returns409() throws Exception {
        String token = registerAndLogin();
        long book = createBook(2, "5.00");
        addItem(token, book, 3).andExpect(status().isConflict());
    }

    @Test
    void addItem_unknownBook_returns404() throws Exception {
        addItem(registerAndLogin(), 999999L, 1).andExpect(status().isNotFound());
    }

    @Test
    void addItem_invalidQuantity_returns400() throws Exception {
        String token = registerAndLogin();
        long book = createBook(2, "5.00");
        addItem(token, book, 0).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.quantity").value("quantity must be at least 1"));
    }

    @Test
    void updateItem_notInCart_returns404() throws Exception {
        mockMvc.perform(put("/api/cart/items/1").header("Authorization", registerAndLogin())
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("quantity", 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkout_emptyCart_returns400() throws Exception {
        mockMvc.perform(post("/api/orders").header("Authorization", registerAndLogin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot place an order: the cart is empty"));
    }

    @Test
    void checkout_stockChangedAfterAdding_returns409() throws Exception {
        String token = registerAndLogin();
        long book = createBook(5, "9.99");
        addItem(token, book, 5).andExpect(status().isCreated());

        // someone else buys 3 first
        String other = registerAndLogin();
        addItem(other, book, 3).andExpect(status().isCreated());
        mockMvc.perform(post("/api/orders").header("Authorization", other)).andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders").header("Authorization", token))
                .andExpect(status().isConflict());
    }

    @Test
    void clearCart_returns204() throws Exception {
        String token = registerAndLogin();
        long book = createBook(5, "1.00");
        addItem(token, book, 1).andExpect(status().isCreated());
        mockMvc.perform(delete("/api/cart").header("Authorization", token)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/cart").header("Authorization", token))
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    private org.springframework.test.web.servlet.ResultActions addItem(String token, long bookId, int quantity)
            throws Exception {
        return mockMvc.perform(post("/api/cart/items").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("bookId", bookId, "quantity", quantity))));
    }
}
