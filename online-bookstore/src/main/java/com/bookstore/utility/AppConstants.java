package com.bookstore.utility;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final int DEFAULT_TOP_BOOKS_LIMIT = 5;
    public static final int MAX_TOP_BOOKS_LIMIT = 50;

    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "Bearer";

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    /** Request attribute used to tell the entry point why the JWT was rejected. */
    public static final String JWT_ERROR_ATTRIBUTE = "bookstore.jwt.error";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
}
