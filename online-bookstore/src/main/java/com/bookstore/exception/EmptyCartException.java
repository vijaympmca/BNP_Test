package com.bookstore.exception;

/** → 400 Bad Request (checkout with an empty cart) */
public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cannot place an order: the cart is empty");
    }
}
