package org.group12.feature.recipe.exception;

import org.group12.common.exception.AppException;

public class RecipeProviderQuotaExceededException extends AppException {
    public RecipeProviderQuotaExceededException() {
        super(ErrorCodeRecipe.RECIPE_PROVIDER_QUOTA_EXCEEDED);
    }
}
