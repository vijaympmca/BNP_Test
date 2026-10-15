package com.bookstore.service;

import com.bookstore.object.request.LoginRequest;
import com.bookstore.object.request.RegisterRequest;
import com.bookstore.object.response.AuthResponse;
import com.bookstore.object.response.UserResponse;

public interface AuthService {

    /** Creates a new account with role USER. */
    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getProfile(String username);
}
