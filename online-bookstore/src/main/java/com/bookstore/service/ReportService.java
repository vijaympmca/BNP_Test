package com.bookstore.service;

import com.bookstore.dto.OrderSummaryDto;
import com.bookstore.dto.TopSellingBookDto;

import java.util.List;

/** ADMIN reports, built from custom JPQL projections in the dto package. */
public interface ReportService {

    List<TopSellingBookDto> getTopSellingBooks(int limit);

    List<OrderSummaryDto> getOrderSummary();
}
