package com.bookstore.configuration;

import com.bookstore.bean.AppProperties;
import com.bookstore.entity.Book;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds one account per role and a sample catalog at startup (only when the tables are empty).
 * Credentials come from app.settings.seed.* in application.yml.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedBooks();
    }

    private void seedUsers() {
        AppProperties.Seed seed = appProperties.seed();
        if (!userRepository.existsByUsernameIgnoreCase(seed.adminUsername())) {
            userRepository.save(User.builder()
                    .fullName("Store Administrator")
                    .username(seed.adminUsername())
                    .email(seed.adminEmail())
                    .password(passwordEncoder.encode(seed.adminPassword()))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build());
            log.info("Seeded ADMIN account '{}'", seed.adminUsername());
        }
        if (!userRepository.existsByUsernameIgnoreCase(seed.userUsername())) {
            userRepository.save(User.builder()
                    .fullName("John Doe")
                    .username(seed.userUsername())
                    .email(seed.userEmail())
                    .password(passwordEncoder.encode(seed.userPassword()))
                    .role(Role.USER)
                    .enabled(true)
                    .build());
            log.info("Seeded USER account '{}'", seed.userUsername());
        }
    }

    private void seedBooks() {
        if (bookRepository.count() > 0) {
            return;
        }
        List<Book> books = List.of(
                book("Effective Java", "Joshua Bloch", "9780134685991", "Best practices for the Java platform.", "45.99", 25),
                book("Clean Code", "Robert C. Martin", "9780132350884", "A handbook of agile software craftsmanship.", "39.50", 30),
                book("Spring in Action", "Craig Walls", "9781617297571", "Covers Spring Boot, Spring MVC and Spring Data.", "49.99", 15),
                book("Java Concurrency in Practice", "Brian Goetz", "9780321349606", "Writing safe concurrent Java programs.", "52.00", 10),
                book("Head First Design Patterns", "Eric Freeman", "9781492078005", "A brain-friendly guide to design patterns.", "44.99", 20),
                book("Refactoring", "Martin Fowler", "9780134757599", "Improving the design of existing code.", "47.25", 12),
                book("The Pragmatic Programmer", "David Thomas", "9780135957059", "Your journey to mastery.", "41.99", 18),
                book("Designing Data-Intensive Applications", "Martin Kleppmann", "9781449373320", "Reliable, scalable and maintainable systems.", "55.00", 8),
                book("Domain-Driven Design", "Eric Evans", "9780321125217", "Tackling complexity in the heart of software.", "58.75", 6),
                book("Microservices Patterns", "Chris Richardson", "9781617294549", "With examples in Java.", "49.00", 9),
                book("Introduction to Algorithms", "Thomas H. Cormen", "9780262046305", "The classic algorithms textbook.", "89.99", 5),
                book("Kubernetes in Action", "Marko Luksa", "9781617293726", "Deploying and managing containerized apps.", "54.99", 0)
        );
        bookRepository.saveAll(books);
        log.info("Seeded {} books", books.size());
    }

    private static Book book(String title, String author, String isbn, String description, String price, int stock) {
        return Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .description(description)
                .price(new BigDecimal(price))
                .stock(stock)
                .build();
    }
}
