package com.bookstore.object.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartItemRequest(
        @NotNull(message = "bookId is required")
        @Positive(message = "bookId must be a positive number")
        Long bookId,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = 99, message = "quantity cannot exceed 99")
        Integer quantity
) {
}
