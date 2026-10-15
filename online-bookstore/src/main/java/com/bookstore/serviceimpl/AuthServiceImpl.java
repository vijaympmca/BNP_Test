package com.bookstore.serviceimpl;

import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.exception.DuplicateResourceException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapping.UserMapper;
import com.bookstore.object.request.LoginRequest;
import com.bookstore.object.request.RegisterRequest;
import com.bookstore.object.response.AuthResponse;
import com.bookstore.object.response.UserResponse;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.security.JwtTokenProvider;
import com.bookstore.service.AuthService;
import com.bookstore.utility.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Override
    public UserResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        // Layer 3 — business rules (layer 4, the DB unique constraints, is the last safety net)
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Username '" + username + "' is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email '" + email + "' is already registered");
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .username(username)
                .email(email)
                .phone(request.phone() == null || request.phone().isBlank() ? null : request.phone())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)              // self-registration never grants ADMIN
                .enabled(true)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // BadCredentialsException → 401, DisabledException → 403 (handled globally)
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail().trim(), request.password()));

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(auth);
        return new AuthResponse(
                token,
                AppConstants.TOKEN_TYPE,
                jwtTokenProvider.getExpirationMs(),
                principal.getUsername(),
                principal.getRole().name(),
                principal.getRole().getDisplayName());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
