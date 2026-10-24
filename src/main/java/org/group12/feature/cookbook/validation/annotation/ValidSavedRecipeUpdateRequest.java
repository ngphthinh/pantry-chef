package org.group12.feature.cookbook.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.group12.feature.cookbook.validation.validator.ValidSavedRecipeUpdateRequestValidator;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidSavedRecipeUpdateRequestValidator.class)
@Documented
public @interface ValidSavedRecipeUpdateRequest {

    String message() default "At least one of the fields (rating, note, favorite) must be provided.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}