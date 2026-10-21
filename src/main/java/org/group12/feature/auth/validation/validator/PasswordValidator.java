package org.group12.feature.auth.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.group12.feature.auth.validation.annotation.ValidPassword;

public class PasswordValidator
        implements ConstraintValidator<ValidPassword, String> {

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9\\s])\\S{8,}$";

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context
    ) {
        if (password == null) {
            return true;
        }

        return password.matches(PASSWORD_REGEX);
    }
}