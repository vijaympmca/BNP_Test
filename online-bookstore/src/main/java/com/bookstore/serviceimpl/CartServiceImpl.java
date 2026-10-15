package com.bookstore.serviceimpl;

import com.bookstore.dto.CartTotalDto;
import com.bookstore.entity.Book;
import com.bookstore.entity.Cart;
import com.bookstore.entity.CartItem;
import com.bookstore.entity.User;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapping.CartMapper;
import com.bookstore.object.request.AddCartItemRequest;
import com.bookstore.object.request.UpdateCartItemRequest;
import com.bookstore.object.response.CartResponse;
import com.bookstore.pojo.StockCheckResult;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    public CartResponse getCart(String username) {
        return cartMapper.toResponse(getOrCreateCart(username));
    }

    @Override
    @Transactional(readOnly = true)
    public CartTotalDto getCartTotal(String username) {
        return cartItemRepository.calculateCartTotal(findUser(username).getId());
    }

    @Override
    public CartResponse addItem(String username, AddCartItemRequest request) {
        // 1. Book must exist (404)
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", request.bookId()));

        // 2. Find or create the user's cart
        Cart cart = getOrCreateCart(username);

        // 3. Increase quantity if already present, otherwise add a new line
        CartItem existing = cart.findItem(book.getId()).orElse(null);
        int newQuantity = (existing == null ? 0 : existing.getQuantity()) + request.quantity();

        // 4. Never more than the stock (409)
        checkStock(book, newQuantity);

        if (existing != null) {
            existing.setQuantity(newQuantity);
        } else {
            cart.addItem(CartItem.builder().book(book).quantity(newQuantity).build());
        }

        // 5. Save and return line totals + grand total
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateQuantity(String username, Long bookId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(username);
        CartItem item = cart.findItem(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + bookId + " is not in your cart"));

        checkStock(item.getBook(), request.quantity());
        item.setQuantity(request.quantity());

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    public void removeItem(String username, Long bookId) {
        Cart cart = getOrCreateCart(username);
        CartItem item = cart.findItem(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + bookId + " is not in your cart"));
        cart.getItems().remove(item);   // orphanRemoval deletes the row
        cartRepository.save(cart);
    }

    @Override
    public void clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private void checkStock(Book book, int requested) {
        StockCheckResult result = new StockCheckResult(book.getId(), book.getTitle(), requested, book.getStock());
        if (!result.sufficient()) {
            throw new InsufficientStockException(result.title(), result.requested(), result.available());
        }
    }

    private Cart getOrCreateCart(String username) {
        User user = findUser(username);
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private User findUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
