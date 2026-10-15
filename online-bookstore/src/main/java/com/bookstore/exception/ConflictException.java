package com.bookstore.exception;

/** → 409 Conflict (operation not allowed in the current state, e.g. deleting an ordered book). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
