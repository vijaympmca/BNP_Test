package com.bookstore.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Entry point. The main class lives in com.bookstore.application, so sibling packages
 * are not scanned by default — every package root is configured explicitly here.
 */
@SpringBootApplication(scanBasePackages = "com.bookstore")
@EnableJpaRepositories(basePackages = "com.bookstore.repository")
@EntityScan(basePackages = "com.bookstore.entity")
@ConfigurationPropertiesScan(basePackages = "com.bookstore.bean")
public class BookstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreApplication.class, args);
    }
}
