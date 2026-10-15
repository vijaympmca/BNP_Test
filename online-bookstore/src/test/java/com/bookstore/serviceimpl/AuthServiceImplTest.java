package com.bookstore.serviceimpl;

import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.exception.DuplicateResourceException;
import com.bookstore.mapping.UserMapper;
import com.bookstore.object.request.RegisterRequest;
import com.bookstore.object.response.UserResponse;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Spy
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private AuthServiceImpl authService;

    private final RegisterRequest request = new RegisterRequest(
            "Ravi Kumar", "ravi_k", "Ravi.K@Example.com", "Secure#Pass1", "Secure#Pass1", "9876543210");

    @Test
    void register_duplicateUsername_throws() {
        when(userRepository.existsByUsernameIgnoreCase("ravi_k")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already taken");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByUsernameIgnoreCase("ravi_k")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("ravi.k@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void register_storesEncodedPassword_lowercaseEmail_andUserRole() {
        when(userRepository.existsByUsernameIgnoreCase("ravi_k")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("ravi.k@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secure#Pass1")).thenReturn("$2a$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = authService.register(request);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("$2a$hash");
        assertThat(saved.getValue().getEmail()).isEqualTo("ravi.k@example.com");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(response.role()).isEqualTo("USER");
    }
}
