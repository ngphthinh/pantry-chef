package org.group12.feature.recipe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.CursorResponse;
import org.group12.feature.recipe.dto.request.ImageUploadRequest;
import org.group12.feature.recipe.dto.request.RecipeCreateRequest;
import org.group12.feature.recipe.dto.request.RecipeUpdateRequest;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.dto.response.RecipeSummaryResponse;
import org.group12.feature.recipe.enums.RecipeResponseCode;
import org.group12.feature.recipe.service.CustomRecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Công thức tự tạo của người dùng. {@code cursor} ở đây là UUID của công thức (dữ liệu trong DB). */
@RestController
@RequestMapping("/v1/recipes/custom")
@RequiredArgsConstructor
public class CustomRecipeController {

    private final CustomRecipeService customRecipeService;

    // POST /v1/recipes/custom
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RecipeDetailResponse> create(@Valid @RequestBody RecipeCreateRequest request) {
        return ApiResponse.<RecipeDetailResponse>builder()
                .code(RecipeResponseCode.CUSTOM_RECIPE_CREATED.getCode())
                .message(RecipeResponseCode.CUSTOM_RECIPE_CREATED.getMessage())
                .data(customRecipeService.create(request))
                .build();
    }

    // GET /v1/recipes/custom?cursor=&size=
    @GetMapping
    public ApiResponse<CursorResponse<RecipeSummaryResponse>> list(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<CursorResponse<RecipeSummaryResponse>>builder()
                .code(RecipeResponseCode.CUSTOM_RECIPES_FETCHED.getCode())
                .message(RecipeResponseCode.CUSTOM_RECIPES_FETCHED.getMessage())
                .data(customRecipeService.getMyRecipes(cursor, size))
                .build();
    }

    // GET /v1/recipes/custom/{id}
    @GetMapping("/{id}")
    public ApiResponse<RecipeDetailResponse> getById(@PathVariable UUID id) {
        return ApiResponse.<RecipeDetailResponse>builder()
                .code(RecipeResponseCode.CUSTOM_RECIPE_FETCHED.getCode())
                .message(RecipeResponseCode.CUSTOM_RECIPE_FETCHED.getMessage())
                .data(customRecipeService.getById(id))
                .build();
    }

    // PUT /v1/recipes/custom/{id}
    @PutMapping("/{id}")
    public ApiResponse<RecipeDetailResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody RecipeUpdateRequest request) {
        return ApiResponse.<RecipeDetailResponse>builder()
                .code(RecipeResponseCode.CUSTOM_RECIPE_UPDATED.getCode())
                .message(RecipeResponseCode.CUSTOM_RECIPE_UPDATED.getMessage())
                .data(customRecipeService.update(id, request))
                .build();
    }

    // DELETE /v1/recipes/custom/{id}
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        customRecipeService.delete(id);
        return ApiResponse.<Void>builder()
                .code(RecipeResponseCode.CUSTOM_RECIPE_DELETED.getCode())
                .message(RecipeResponseCode.CUSTOM_RECIPE_DELETED.getMessage())
                .build();
    }

    // POST /v1/recipes/custom/{id}/image
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<RecipeDetailResponse> uploadImage(@PathVariable UUID id,
                                                         @Valid @ModelAttribute ImageUploadRequest request) {
        return ApiResponse.<RecipeDetailResponse>builder()
                .code(RecipeResponseCode.CUSTOM_RECIPE_IMAGE_UPLOADED.getCode())
                .message(RecipeResponseCode.CUSTOM_RECIPE_IMAGE_UPLOADED.getMessage())
                .data(customRecipeService.uploadImage(id, request))
                .build();
    }
}
