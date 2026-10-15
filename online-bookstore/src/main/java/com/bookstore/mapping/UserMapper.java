package com.bookstore.mapping;

import com.bookstore.entity.User;
import com.bookstore.object.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /** The password hash is intentionally not mapped. */
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getRole().getDisplayName(),
                user.isEnabled(),
                user.getCreatedAt());
    }
}
