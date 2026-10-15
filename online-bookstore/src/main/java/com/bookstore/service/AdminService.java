package com.bookstore.service;

import com.bookstore.entity.Role;
import com.bookstore.object.response.PageResponse;
import com.bookstore.object.response.UserResponse;

/** User management, available to the ADMIN (Administrator) role only. */
public interface AdminService {

    PageResponse<UserResponse> getUsers(int page, int size);

    UserResponse getUser(Long id);

    UserResponse changeRole(String currentAdmin, Long userId, Role role);

    UserResponse changeStatus(String currentAdmin, Long userId, boolean enabled);
}
