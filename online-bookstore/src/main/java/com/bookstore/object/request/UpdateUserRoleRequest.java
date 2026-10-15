package com.bookstore.object.request;

import com.bookstore.entity.Role;
import jakarta.validation.constraints.NotNull;

/** ADMIN only: change a user's role. Accepted values: "USER" or "ADMIN". */
public record UpdateUserRoleRequest(
        @NotNull(message = "role is required (USER or ADMIN)")
        Role role
) {
}
