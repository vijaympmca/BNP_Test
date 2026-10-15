package com.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Read-only projection returned by OrderItemRepository.findTopSellingBooks. */
@JsonIgnoreProperties({"target", "targetClass", "decoratedClass"})
public interface TopSellingBookDto {
    Long getBookId();

    String getTitle();

    String getAuthor();

    Long getTotalSold();

    BigDecimal getTotalRevenue();
}
