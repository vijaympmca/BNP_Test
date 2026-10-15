package com.bookstore.object.request;

import jakarta.validation.constraints.NotNull;

/** ADMIN only: enable or disable a user account. */
public record UpdateUserStatusRequest(
        @NotNull(message = "enabled is required (true or false)")
        Boolean enabled
) {
}
