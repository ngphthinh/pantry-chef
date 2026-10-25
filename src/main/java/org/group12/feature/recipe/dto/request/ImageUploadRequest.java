package org.group12.feature.recipe.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.validation.annotation.ValidImage;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Request object for uploading an image for a custom recipe")
public class ImageUploadRequest {
    @ValidImage
    private MultipartFile image;
}
