package com.bookstore.controller;

import com.bookstore.object.response.ApiResponse;
import com.bookstore.object.response.OrderResponse;
import com.bookstore.object.response.PageResponse;
import com.bookstore.service.OrderService;
import com.bookstore.utility.AppConstants;
import com.bookstore.utility.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Orders", description = "Checkout and order history of the logged-in user")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place an order from my cart")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> checkout() {
        OrderResponse order = orderService.checkout(SecurityUtils.getCurrentUsername());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(order.id()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.success("Order placed successfully", order));
    }

    @Operation(summary = "My orders, newest first")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved",
                orderService.getMyOrders(SecurityUtils.getCurrentUsername(), page, size)));
    }

    @Operation(summary = "One order (owner or ADMIN)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved",
                orderService.getOrder(SecurityUtils.getCurrentUsername(), SecurityUtils.isAdmin(), id)));
    }

    @Operation(summary = "Cancel a placed order and return its stock (owner or ADMIN)")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
                orderService.cancelOrder(SecurityUtils.getCurrentUsername(), SecurityUtils.isAdmin(), id)));
    }
}
