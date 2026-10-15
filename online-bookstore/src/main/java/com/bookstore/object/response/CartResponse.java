package com.bookstore.object.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        List<CartItemResponse> items,
        int distinctItems,
        int totalQuantity,
        BigDecimal grandTotal) {
}
