package com.bookstore.exception;

/** → 409 Conflict (requested quantity is more than the stock) */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String title, int requested, int available) {
        super("Insufficient stock for '" + title + "': requested " + requested + ", available " + available);
    }
}
