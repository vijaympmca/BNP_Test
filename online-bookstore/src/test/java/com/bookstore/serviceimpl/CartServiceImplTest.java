package com.bookstore.serviceimpl;

import com.bookstore.entity.Book;
import com.bookstore.entity.Cart;
import com.bookstore.entity.CartItem;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapping.CartMapper;
import com.bookstore.object.request.AddCartItemRequest;
import com.bookstore.object.response.CartResponse;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private CartMapper cartMapper = new CartMapper();

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Book book;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("ravi_k").role(Role.USER).build();
        book = Book.builder().id(10L).title("Effective Java").author("Joshua Bloch")
                .price(new BigDecimal("45.99")).stock(5).build();
        cart = Cart.builder().id(100L).user(user).build();
    }

    @Test
    void addItem_unknownBook_throwsNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem("ravi_k", new AddCartItemRequest(99L, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addItem_newBook_addsLineWithTotals() {
        stubCart();
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.addItem("ravi_k", new AddCartItemRequest(10L, 2));

        assertThat(response.items()).hasSize(1);
        assertThat(response.totalQuantity()).isEqualTo(2);
        assertThat(response.grandTotal()).isEqualByComparingTo("91.98");
    }

    @Test
    void addItem_existingBook_increasesQuantity() {
        cart.addItem(CartItem.builder().book(book).quantity(2).build());
        stubCart();
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.addItem("ravi_k", new AddCartItemRequest(10L, 3));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void addItem_exceedingStock_throwsInsufficientStock() {
        cart.addItem(CartItem.builder().book(book).quantity(4).build());
        stubCart();

        assertThatThrownBy(() -> cartService.addItem("ravi_k", new AddCartItemRequest(10L, 2)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("requested 6, available 5");
    }

    @Test
    void removeItem_notInCart_throwsNotFound() {
        when(userRepository.findByUsernameIgnoreCase("ravi_k")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.removeItem("ravi_k", 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void stubCart() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(userRepository.findByUsernameIgnoreCase("ravi_k")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    }
}
