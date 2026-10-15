package com.bookstore.object.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** To remove an item use DELETE /api/cart/items/{bookId}; quantity here must be 1–99. */
public record UpdateCartItemRequest(
        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = 99, message = "quantity cannot exceed 99")
        Integer quantity
) {
}
