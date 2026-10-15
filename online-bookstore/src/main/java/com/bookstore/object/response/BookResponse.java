package com.bookstore.object.response;

import java.math.BigDecimal;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        String description,
        BigDecimal price,
        Integer stock,
        boolean inStock,
        String imageUrl) {
}
