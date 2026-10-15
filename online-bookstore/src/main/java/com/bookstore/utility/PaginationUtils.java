package com.bookstore.utility;

import com.bookstore.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Set;

/** Validates paging input and builds a PageRequest (bad values → 400). */
public final class PaginationUtils {

    private PaginationUtils() {
    }

    public static PageRequest of(int page, int size, int maxSize) {
        return of(page, size, maxSize, Sort.unsorted());
    }

    public static PageRequest of(int page, int size, int maxSize, Sort sort) {
        if (page < 0) {
            throw new BadRequestException("page must be 0 or greater");
        }
        if (size < 1 || size > maxSize) {
            throw new BadRequestException("size must be between 1 and " + maxSize);
        }
        return PageRequest.of(page, size, sort);
    }

    public static Sort sort(String sortBy, String direction, Set<String> allowedFields) {
        if (!allowedFields.contains(sortBy)) {
            throw new BadRequestException("sortBy must be one of " + allowedFields);
        }
        Sort.Direction dir;
        if ("asc".equalsIgnoreCase(direction)) {
            dir = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(direction)) {
            dir = Sort.Direction.DESC;
        } else {
            throw new BadRequestException("direction must be 'asc' or 'desc'");
        }
        return Sort.by(dir, sortBy);
    }
}
