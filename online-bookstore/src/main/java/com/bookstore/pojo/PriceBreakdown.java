package com.bookstore.pojo;

import java.math.BigDecimal;

/** Totals of a cart or order, calculated in memory by PriceUtils. */
public record PriceBreakdown(int distinctItems, int totalQuantity, BigDecimal grandTotal) {
}
