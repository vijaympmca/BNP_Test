package com.bookstore.mapping;

import com.bookstore.entity.Book;
import com.bookstore.entity.Cart;
import com.bookstore.entity.CartItem;
import com.bookstore.object.response.CartItemResponse;
import com.bookstore.object.response.CartResponse;
import com.bookstore.pojo.PriceBreakdown;
import com.bookstore.utility.PriceUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        List<CartItem> sorted = cart.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getBook().getTitle()))
                .toList();
        PriceBreakdown totals = PriceUtils.breakdown(sorted);
        return new CartResponse(
                cart.getId(),
                sorted.stream().map(this::toItemResponse).toList(),
                totals.distinctItems(),
                totals.totalQuantity(),
                totals.grandTotal());
    }

    public CartItemResponse toItemResponse(CartItem item) {
        Book book = item.getBook();
        return new CartItemResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                PriceUtils.scale(book.getPrice()),
                item.getQuantity(),
                PriceUtils.lineTotal(book.getPrice(), item.getQuantity()),
                book.getStock());
    }
}
