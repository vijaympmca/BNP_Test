package com.bookstore.dto;

import com.bookstore.entity.OrderStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Read-only projection returned by OrderRepository.summarizeByStatus. */
@JsonIgnoreProperties({"target", "targetClass", "decoratedClass"})
public interface OrderSummaryDto {
    OrderStatus getStatus();

    Long getOrderCount();

    BigDecimal getTotalRevenue();
}
