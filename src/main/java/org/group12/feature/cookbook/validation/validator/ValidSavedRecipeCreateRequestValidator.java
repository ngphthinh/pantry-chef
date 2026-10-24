package org.group12.feature.cookbook.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.group12.feature.cookbook.dto.request.SavedRecipeCreateRequest;
import org.group12.feature.cookbook.validation.annotation.ValidSavedRecipeCreateRequest;

public class ValidSavedRecipeCreateRequestValidator
        implements ConstraintValidator<ValidSavedRecipeCreateRequest, SavedRecipeCreateRequest> {

    @Override
    public boolean isValid(
            SavedRecipeCreateRequest request,
            ConstraintValidatorContext context
    ) {
        if (request == null) {
            return true;
        }

        boolean hasExternal = request.getExternalId() != null
                && !request.getExternalId().toString().isBlank();

        boolean hasRecipeId = request.getRecipeId() != null;

        return hasExternal ^ hasRecipeId;
    }
}