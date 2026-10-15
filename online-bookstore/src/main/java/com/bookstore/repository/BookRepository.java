package com.bookstore.repository;

import com.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    /** Case-insensitive search on title or author. */
    @Query("""
           SELECT b FROM Book b
           WHERE LOWER(b.title)  LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%'))
           """)
    Page<Book> search(@Param("search") String search, Pageable pageable);
}
