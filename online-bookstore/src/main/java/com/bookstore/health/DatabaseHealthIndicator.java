package com.bookstore.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/** Shown as "database" under /actuator/health. DOWN → overall 503. */
@Component("database")
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                return Health.up()
                        .withDetail("database", connection.getMetaData().getDatabaseProductName())
                        .withDetail("url", connection.getMetaData().getURL())
                        .build();
            }
            return Health.down().withDetail("reason", "Connection is not valid").build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}
