package com.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Read-only projection returned by CartItemRepository.calculateCartTotal. */
@JsonIgnoreProperties({"target", "targetClass", "decoratedClass"})
public interface CartTotalDto {
    Long getItemCount();

    Long getTotalQuantity();

    BigDecimal getTotalAmount();
}
