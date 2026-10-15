package com.bookstore.pojo;

/** Result of comparing a requested quantity against a book's stock. */
public record StockCheckResult(Long bookId, String title, int requested, int available) {

    public boolean sufficient() {
        return requested <= available;
    }
}
