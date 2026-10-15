package com.bookstore.repository;

import com.bookstore.dto.OrderSummaryDto;
import com.bookstore.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Number of orders and revenue per status (ADMIN report). Entity name is PurchaseOrder. */
    @Query("""
           SELECT o.status           AS status,
                  COUNT(o)           AS orderCount,
                  SUM(o.totalAmount) AS totalRevenue
           FROM PurchaseOrder o
           GROUP BY o.status
           ORDER BY o.status
           """)
    List<OrderSummaryDto> summarizeByStatus();
}
