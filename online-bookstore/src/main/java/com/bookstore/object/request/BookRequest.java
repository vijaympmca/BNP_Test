package com.bookstore.object.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** ADMIN only: body for creating or updating a book. */
public record BookRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Author is required")
        @Size(max = 100, message = "Author must not exceed 100 characters")
        String author,

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^(\\d{9}[\\dX]|97[89]\\d{10})$",
                message = "ISBN must be a 10-character ISBN-10 or a 13-digit ISBN-13 (digits only, no hyphens)")
        String isbn,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
        @DecimalMax(value = "99999999.99", message = "Price is too large")
        @Digits(integer = 8, fraction = 2, message = "Price can have at most 2 decimal places")
        BigDecimal price,

        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        @Max(value = 100000, message = "Stock cannot exceed 100000")
        Integer stock,

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        @Pattern(regexp = "^(https?://.*)?$", message = "Image URL must start with http:// or https://")
        String imageUrl
) {
}
