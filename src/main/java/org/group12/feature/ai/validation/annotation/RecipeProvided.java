package org.group12.feature.ai.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.group12.feature.ai.validation.validator.RecipeProvidedValidator;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RecipeProvidedValidator.class)
@Documented
public @interface RecipeProvided {

    String message() default "Recipe must be provided if the recipeId is null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}