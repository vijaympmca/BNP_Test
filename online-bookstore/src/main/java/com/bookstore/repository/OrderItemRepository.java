package com.bookstore.repository;

import com.bookstore.dto.TopSellingBookDto;
import com.bookstore.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    boolean existsByBookId(Long bookId);

    /** Best-selling books, counting only orders that were not cancelled. */
    @Query("""
           SELECT oi.book.id                         AS bookId,
                  oi.book.title                      AS title,
                  oi.book.author                     AS author,
                  SUM(oi.quantity)                   AS totalSold,
                  SUM(oi.quantity * oi.priceAtPurchase) AS totalRevenue
           FROM OrderItem oi
           WHERE oi.order.status = com.bookstore.entity.OrderStatus.PLACED
           GROUP BY oi.book.id, oi.book.title, oi.book.author
           ORDER BY SUM(oi.quantity) DESC
           """)
    List<TopSellingBookDto> findTopSellingBooks(Pageable pageable);
}
