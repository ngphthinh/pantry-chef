package org.group12.feature.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.ai.validation.annotation.RecipeProvided;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@RecipeProvided
@Schema(description = "Request body for ingredient substitution")
public class SubstitutionRequest {

    @Schema(description = "UUID của công thức trong DB (custom hoặc đã cache từ API ngoài). Bỏ trống nếu gửi recipeTitle.")
    private UUID recipeId;

    @Schema(description = "Tên món. Ưu tiên hơn recipeId, không tra DB.", example = "Phở bò")
    @Size(max = 200)
    private String recipeTitle;

    @Schema(description = "Tên nguyên liệu cần thay thế", example = "Bơ")
    @NotBlank
    @Size(max = 100)
    private String missingIngredient;

}