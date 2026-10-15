package com.bookstore.controller;

import com.bookstore.dto.OrderSummaryDto;
import com.bookstore.dto.TopSellingBookDto;
import com.bookstore.object.response.ApiResponse;
import com.bookstore.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Reports (ADMIN)", description = "Sales reports for administrators")
@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Best-selling books (cancelled orders excluded)")
    @GetMapping("/top-books")
    public ResponseEntity<ApiResponse<List<TopSellingBookDto>>> topBooks(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success("Top selling books", reportService.getTopSellingBooks(limit)));
    }

    @Operation(summary = "Order count and revenue per status")
    @GetMapping("/order-summary")
    public ResponseEntity<ApiResponse<List<OrderSummaryDto>>> orderSummary() {
        return ResponseEntity.ok(ApiResponse.success("Order summary", reportService.getOrderSummary()));
    }
}
