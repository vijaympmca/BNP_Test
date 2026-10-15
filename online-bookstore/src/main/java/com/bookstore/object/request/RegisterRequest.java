package com.bookstore.object.request;

import com.bookstore.utility.validator.PasswordMatches;
import com.bookstore.utility.validator.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Public registration always creates a USER. Administrators are seeded at startup
 * or promoted by another ADMIN through PATCH /api/admin/users/{id}/role.
 */
@PasswordMatches
public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 50, message = "Full name must be 2 to 50 characters")
        @Pattern(regexp = "^[A-Za-z ]+$", message = "Full name can contain only letters and spaces")
        String fullName,

        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 20, message = "Username must be 4 to 20 characters")
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9._]*$",
                message = "Username must start with a letter and contain only letters, digits, '.' or '_'")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @StrongPassword
        String password,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword,

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit number starting with 6-9")
        String phone   // optional: @Pattern ignores null
) {
}
