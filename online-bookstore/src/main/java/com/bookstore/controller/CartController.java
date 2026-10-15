package com.bookstore.controller;

import com.bookstore.dto.CartTotalDto;
import com.bookstore.object.request.AddCartItemRequest;
import com.bookstore.object.request.UpdateCartItemRequest;
import com.bookstore.object.response.ApiResponse;
import com.bookstore.object.response.CartResponse;
import com.bookstore.service.CartService;
import com.bookstore.utility.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The cart always belongs to the user in the JWT — there is no user id in any URL. */
@Tag(name = "Cart", description = "Shopping cart of the logged-in user (USER or ADMIN)")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "View my cart")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved",
                cartService.getCart(SecurityUtils.getCurrentUsername())));
    }

    @Operation(summary = "Cart totals calculated in the database")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CartTotalDto>> getCartSummary() {
        return ResponseEntity.ok(ApiResponse.success("Cart summary",
                cartService.getCartTotal(SecurityUtils.getCurrentUsername())));
    }

    @Operation(summary = "Add a book to my cart (adds to the quantity if already present)")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody AddCartItemRequest request) {
        CartResponse cart = cartService.addItem(SecurityUtils.getCurrentUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Item added to cart", cart));
    }

    @Operation(summary = "Change the quantity of a book in my cart")
    @PutMapping("/items/{bookId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(@PathVariable Long bookId,
                                                                    @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cart item updated",
                cartService.updateQuantity(SecurityUtils.getCurrentUsername(), bookId, request)));
    }

    @Operation(summary = "Remove a book from my cart")
    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long bookId) {
        cartService.removeItem(SecurityUtils.getCurrentUsername(), bookId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Empty my cart")
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(SecurityUtils.getCurrentUsername());
        return ResponseEntity.noContent().build();
    }
}
