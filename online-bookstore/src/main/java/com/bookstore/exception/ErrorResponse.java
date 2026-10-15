package com.bookstore.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/** Standard error body returned for every failed request. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors) {

    /** Factory method — keeps every error body consistent. */
    public static ErrorResponse of(HttpStatus status, String message, String path, Map<String, String> validationErrors) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, path, validationErrors == null || validationErrors.isEmpty() ? null : validationErrors);
    }

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return of(status, message, path, null);
    }
}
