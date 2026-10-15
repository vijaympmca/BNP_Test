package com.bookstore.object.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long bookId,
        String title,
        Integer quantity,
        BigDecimal priceAtPurchase,
        BigDecimal lineTotal) {
}
