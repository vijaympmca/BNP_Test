package com.bookstore.utility;

import com.bookstore.entity.CartItem;
import com.bookstore.pojo.PriceBreakdown;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Money is always BigDecimal with 2 decimals, rounded HALF_UP. */
public final class PriceUtils {

    private PriceUtils() {
    }

    public static BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        return scale(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    public static PriceBreakdown breakdown(List<CartItem> items) {
        int totalQuantity = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            totalQuantity += item.getQuantity();
            total = total.add(lineTotal(item.getBook().getPrice(), item.getQuantity()));
        }
        return new PriceBreakdown(items.size(), totalQuantity, scale(total));
    }
}
