package org.group12.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.group12.common.validation.annotation.ValidImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class ImageValidator
        implements ConstraintValidator<ValidImage, MultipartFile> {

    @Value("${app.upload.max-size}")
    private long maxSize;

    @Value("${app.upload.allowed-types}")
    private Set<String> allowedTypes;

    @Override
    public boolean isValid(
            MultipartFile image,
            ConstraintValidatorContext context) {

        if (image == null || image.isEmpty()) {
            return addViolation(context, "Ảnh trống");
        }

        if (image.getSize() > maxSize) {
            return addViolation(context, "Ảnh tối đa 5MB");
        }

        if (image.getContentType() == null
                || !allowedTypes.contains(image.getContentType())) {
            return addViolation(
                context, "Chỉ hỗ trợ JPEG/PNG/WebP"
            );
        }

        return true;
    }

    private boolean addViolation(
            ConstraintValidatorContext context,
            String message) {

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();

        return false;
    }
}