package com.bookstore.object.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Stable paging contract (Spring's Page JSON format is not guaranteed to stay the same). */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    /** Factory method: converts a Page of entities into a PageResponse of response objects. */
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
