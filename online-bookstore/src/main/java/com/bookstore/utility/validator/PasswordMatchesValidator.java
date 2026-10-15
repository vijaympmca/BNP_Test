package com.bookstore.utility.validator;

import com.bookstore.object.request.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest req, ConstraintValidatorContext context) {
        if (req == null || req.password() == null || req.confirmPassword() == null) {
            return true;   // @NotBlank handles missing values
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (!req.password().equals(req.confirmPassword())) {
            context.buildConstraintViolationWithTemplate("Password and confirm password do not match")
                    .addPropertyNode("confirmPassword").addConstraintViolation();
            valid = false;
        }
        if (req.username() != null && !req.username().isBlank()
                && req.password().toLowerCase().contains(req.username().trim().toLowerCase())) {
            context.buildConstraintViolationWithTemplate("Password must not contain the username")
                    .addPropertyNode("password").addConstraintViolation();
            valid = false;
        }
        return valid;
    }
}
