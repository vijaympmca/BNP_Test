package com.bookstore.repository;

import com.bookstore.dto.CartTotalDto;
import com.bookstore.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /** Totals of a user's cart, calculated in the database (DTO projection). */
    @Query("""
           SELECT COUNT(ci)                                     AS itemCount,
                  COALESCE(SUM(ci.quantity), 0)                 AS totalQuantity,
                  COALESCE(SUM(ci.quantity * ci.book.price), 0) AS totalAmount
           FROM CartItem ci
           WHERE ci.cart.user.id = :userId
           """)
    CartTotalDto calculateCartTotal(@Param("userId") Long userId);

    /** Used when an ADMIN deletes a book: remove it from every cart first. */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.book.id = :bookId")
    int deleteByBookId(@Param("bookId") Long bookId);
}
