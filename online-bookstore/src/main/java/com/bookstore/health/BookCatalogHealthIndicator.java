package com.bookstore.health;

import com.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** Shown as "bookCatalog" under /actuator/health: number of books in the catalog. */
@Component("bookCatalog")
@RequiredArgsConstructor
public class BookCatalogHealthIndicator implements HealthIndicator {

    private final BookRepository bookRepository;

    @Override
    public Health health() {
        try {
            long count = bookRepository.count();
            return Health.up()
                    .withDetail("totalBooks", count)
                    .withDetail("catalogEmpty", count == 0)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}
