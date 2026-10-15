package com.bookstore.service;

import com.bookstore.object.request.BookRequest;
import com.bookstore.object.response.BookResponse;
import com.bookstore.object.response.PageResponse;

public interface BookService {

    PageResponse<BookResponse> getBooks(int page, int size, String search, String sortBy, String direction);

    BookResponse getBook(Long id);

    BookResponse createBook(BookRequest request);       // ADMIN

    BookResponse updateBook(Long id, BookRequest request); // ADMIN

    void deleteBook(Long id);                           // ADMIN
}
