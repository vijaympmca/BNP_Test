package com.bookstore.service;

import com.bookstore.object.response.OrderResponse;
import com.bookstore.object.response.PageResponse;

public interface OrderService {

    /** Turns the user's cart into an order (Facade over cart, stock and order). */
    OrderResponse checkout(String username);

    PageResponse<OrderResponse> getMyOrders(String username, int page, int size);

    /** Owner or ADMIN can view; anyone else gets 403. */
    OrderResponse getOrder(String username, boolean isAdmin, Long orderId);

    /** Owner or ADMIN can cancel a PLACED order; stock is returned. */
    OrderResponse cancelOrder(String username, boolean isAdmin, Long orderId);

    PageResponse<OrderResponse> getAllOrders(int page, int size); // ADMIN
}
