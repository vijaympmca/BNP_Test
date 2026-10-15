package com.bookstore.object.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/** Envelope for every successful response. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data, LocalDateTime timestamp) {

    /** Factory method. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }
}
