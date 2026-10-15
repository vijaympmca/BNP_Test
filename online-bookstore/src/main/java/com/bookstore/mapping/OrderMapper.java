package com.bookstore.mapping;

import com.bookstore.entity.Order;
import com.bookstore.entity.OrderItem;
import com.bookstore.object.response.OrderItemResponse;
import com.bookstore.object.response.OrderResponse;
import com.bookstore.utility.PriceUtils;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getUsername(),
                order.getStatus().name(),
                PriceUtils.scale(order.getTotalAmount()),
                order.getCreatedAt(),
                order.getItems().stream().map(this::toItemResponse).toList());
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getBook().getId(),
                item.getBookTitle(),
                item.getQuantity(),
                PriceUtils.scale(item.getPriceAtPurchase()),
                PriceUtils.lineTotal(item.getPriceAtPurchase(), item.getQuantity()));
    }
}
