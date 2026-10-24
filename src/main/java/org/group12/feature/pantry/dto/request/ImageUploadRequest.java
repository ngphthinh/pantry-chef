package org.group12.feature.pantry.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.validation.annotation.ValidImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Request object for uploading an image for a pantry item")
public class ImageUploadRequest {
    @ValidImage
    private MultipartFile image;

}