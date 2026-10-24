package org.group12.feature.cookbook.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.group12.feature.cookbook.validation.validator.ValidSavedRecipeCreateRequestValidator;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidSavedRecipeCreateRequestValidator.class)
@Documented
public @interface ValidSavedRecipeCreateRequest {

    String message() default "Missing required fields: externalId or recipeId must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}