package com.bookstore.serviceimpl;

import com.bookstore.bean.AppProperties;
import com.bookstore.entity.Book;
import com.bookstore.entity.Cart;
import com.bookstore.entity.CartItem;
import com.bookstore.entity.Order;
import com.bookstore.entity.OrderItem;
import com.bookstore.entity.OrderStatus;
import com.bookstore.entity.User;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.EmptyCartException;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapping.OrderMapper;
import com.bookstore.object.response.OrderResponse;
import com.bookstore.object.response.PageResponse;
import com.bookstore.pojo.PriceBreakdown;
import com.bookstore.pojo.StockCheckResult;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.OrderService;
import com.bookstore.utility.PaginationUtils;
import com.bookstore.utility.PriceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final AppProperties appProperties;

    /**
     * Facade: one call coordinates cart, stock and order. Runs in a single transaction,
     * so any failure rolls everything back. Book.@Version guards against concurrent checkouts.
     */
    @Override
    public OrderResponse checkout(String username) {
        User user = findUser(username);

        // 1. Load cart; empty → 400
        Cart cart = cartRepository.findByUserId(user.getId())
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(EmptyCartException::new);

        // 2. Re-check stock for every item (it may have changed since items were added) → 409
        for (CartItem item : cart.getItems()) {
            Book book = item.getBook();
            StockCheckResult check = new StockCheckResult(book.getId(), book.getTitle(), item.getQuantity(), book.getStock());
            if (!check.sufficient()) {
                throw new InsufficientStockException(check.title(), check.requested(), check.available());
            }
        }

        // 3. Create order + items with a price snapshot
        PriceBreakdown totals = PriceUtils.breakdown(cart.getItems());
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PLACED)
                .totalAmount(totals.grandTotal())
                .build();

        for (CartItem item : cart.getItems()) {
            Book book = item.getBook();
            order.addItem(OrderItem.builder()
                    .book(book)
                    .bookTitle(book.getTitle())
                    .quantity(item.getQuantity())
                    .priceAtPurchase(PriceUtils.scale(book.getPrice()))
                    .build());

            // 4. Decrease stock
            book.setStock(book.getStock() - item.getQuantity());
        }

        Order saved = orderRepository.save(order);

        // 5. Clear the cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(String username, int page, int size) {
        User user = findUser(username);
        return PageResponse.from(
                orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(),
                        PaginationUtils.of(page, size, appProperties.maxPageSize())),
                orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String username, boolean isAdmin, Long orderId) {
        return orderMapper.toResponse(findOwnedOrder(username, isAdmin, orderId, "view"));
    }

    @Override
    public OrderResponse cancelOrder(String username, boolean isAdmin, Long orderId) {
        Order order = findOwnedOrder(username, isAdmin, orderId, "cancel");
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("Order " + orderId + " is already cancelled");
        }
        // Return the stock
        for (OrderItem item : order.getItems()) {
            Book book = item.getBook();
            book.setStock(book.getStock() + item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(int page, int size) {
        return PageResponse.from(
                orderRepository.findAllByOrderByCreatedAtDesc(PaginationUtils.of(page, size, appProperties.maxPageSize())),
                orderMapper::toResponse);
    }

    /** Ownership validation: only the owner or an ADMIN may access an order (otherwise 403). */
    private Order findOwnedOrder(String username, boolean isAdmin, Long orderId, String action) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!isAdmin && !order.getUser().getUsername().equalsIgnoreCase(username)) {
            throw new AccessDeniedException("You are not allowed to " + action + " this order");
        }
        return order;
    }

    private User findUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
