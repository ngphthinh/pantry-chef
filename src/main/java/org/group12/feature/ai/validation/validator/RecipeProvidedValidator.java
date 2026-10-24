package org.group12.feature.ai.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.group12.feature.ai.dto.request.SubstitutionRequest;
import org.group12.feature.ai.validation.annotation.RecipeProvided;

public class RecipeProvidedValidator
        implements ConstraintValidator<RecipeProvided, SubstitutionRequest> {

    @Override
    public boolean isValid(
            SubstitutionRequest request,
            ConstraintValidatorContext context) {

        if (request == null) {
            return true;
        }

        boolean hasTitle = request.getRecipeTitle() != null
                && !request.getRecipeTitle().isBlank();

        boolean hasId = request.getRecipeId() != null
                && !request.getRecipeId().toString().isBlank();

        return hasTitle || hasId;
    }
}