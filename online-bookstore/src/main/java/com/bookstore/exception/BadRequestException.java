package com.bookstore.exception;

/** → 400 Bad Request (invalid input that annotations can't express, e.g. bad paging) */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
