package org.group12.feature.cookbook.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.group12.feature.cookbook.dto.request.SavedRecipeUpdateRequest;
import org.group12.feature.cookbook.validation.annotation.ValidSavedRecipeUpdateRequest;

public class ValidSavedRecipeUpdateRequestValidator
        implements ConstraintValidator<ValidSavedRecipeUpdateRequest, SavedRecipeUpdateRequest> {

    @Override
    public boolean isValid(SavedRecipeUpdateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        return request.getRating() != null
                || request.getNote() != null
                || request.getFavorite() != null;
    }
}