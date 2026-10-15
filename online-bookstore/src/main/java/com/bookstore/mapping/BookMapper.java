package com.bookstore.mapping;

import com.bookstore.entity.Book;
import com.bookstore.object.request.BookRequest;
import com.bookstore.object.response.BookResponse;
import com.bookstore.utility.PriceUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/** Hand-written mapper (no code generation needed, easy to read and debug). */
@Component
public class BookMapper {

    public BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getDescription(),
                PriceUtils.scale(book.getPrice()),
                book.getStock(),
                book.getStock() != null && book.getStock() > 0,
                book.getImageUrl());
    }

    public List<BookResponse> toResponseList(List<Book> books) {
        return books.stream().map(this::toResponse).toList();
    }

    public Book toEntity(BookRequest request) {
        Book book = new Book();
        updateEntity(book, request);
        return book;
    }

    public void updateEntity(Book book, BookRequest request) {
        book.setTitle(request.title().trim());
        book.setAuthor(request.author().trim());
        book.setIsbn(request.isbn().trim());
        book.setDescription(request.description());
        book.setPrice(PriceUtils.scale(request.price()));
        book.setStock(request.stock());
        book.setImageUrl(request.imageUrl());
    }
}
