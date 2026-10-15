package com.bookstore.exception;

/** → 409 Conflict (username, email or ISBN already exists) */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
