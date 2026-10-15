package com.bookstore.object.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long bookId,
        String title,
        String author,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal,
        Integer availableStock) {
}
