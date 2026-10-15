package com.bookstore.service;

import com.bookstore.dto.CartTotalDto;
import com.bookstore.object.request.AddCartItemRequest;
import com.bookstore.object.request.UpdateCartItemRequest;
import com.bookstore.object.response.CartResponse;

public interface CartService {

    CartResponse getCart(String username);

    CartTotalDto getCartTotal(String username);

    CartResponse addItem(String username, AddCartItemRequest request);

    CartResponse updateQuantity(String username, Long bookId, UpdateCartItemRequest request);

    void removeItem(String username, Long bookId);

    void clearCart(String username);
}
