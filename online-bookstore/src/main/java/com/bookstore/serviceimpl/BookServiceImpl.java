package com.bookstore.serviceimpl;

import com.bookstore.bean.AppProperties;
import com.bookstore.entity.Book;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.DuplicateResourceException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapping.BookMapper;
import com.bookstore.object.request.BookRequest;
import com.bookstore.object.response.BookResponse;
import com.bookstore.object.response.PageResponse;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.OrderItemRepository;
import com.bookstore.service.BookService;
import com.bookstore.utility.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "title", "author", "price", "stock");

    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookMapper bookMapper;
    private final AppProperties appProperties;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> getBooks(int page, int size, String search, String sortBy, String direction) {
        PageRequest pageable = PaginationUtils.of(page, size, appProperties.maxPageSize(),
                PaginationUtils.sort(sortBy, direction, SORTABLE_FIELDS));

        Page<Book> books = (search == null || search.isBlank())
                ? bookRepository.findAll(pageable)
                : bookRepository.search(search.trim(), pageable);

        return PageResponse.from(books, bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBook(Long id) {
        return bookMapper.toResponse(findBook(id));
    }

    @Override
    public BookResponse createBook(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn().trim())) {
            throw new DuplicateResourceException("A book with ISBN '" + request.isbn() + "' already exists");
        }
        return bookMapper.toResponse(bookRepository.save(bookMapper.toEntity(request)));
    }

    @Override
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findBook(id);
        if (bookRepository.existsByIsbnAndIdNot(request.isbn().trim(), id)) {
            throw new DuplicateResourceException("A book with ISBN '" + request.isbn() + "' already exists");
        }
        bookMapper.updateEntity(book, request);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    public void deleteBook(Long id) {
        Book book = findBook(id);
        if (orderItemRepository.existsByBookId(id)) {
            throw new ConflictException("Book '" + book.getTitle()
                    + "' cannot be deleted because it is part of existing orders. Set its stock to 0 instead");
        }
        cartItemRepository.deleteByBookId(id);
        bookRepository.delete(book);
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book", id));
    }
}
