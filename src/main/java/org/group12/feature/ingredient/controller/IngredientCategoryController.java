package org.group12.feature.ingredient.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.ingredient.dto.request.IngredientCategoryRequest;
import org.group12.feature.ingredient.dto.response.IngredientCategoryResponse;
import org.group12.feature.ingredient.enums.IngredientResponseCode;
import org.group12.feature.ingredient.service.IngredientCategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ingredient-categories")
@RequiredArgsConstructor
public class IngredientCategoryController {

    private final IngredientCategoryService categoryService;

    // GET /v1/ingredient-categories  (công khai, khai báo trong SecurityConfig)
    @GetMapping
    public ApiResponse<List<IngredientCategoryResponse>> getAll() {
        return ApiResponse.<List<IngredientCategoryResponse>>builder()
                .code(IngredientResponseCode.CATEGORY_LIST_SUCCESS.getCode())
                .message(IngredientResponseCode.CATEGORY_LIST_SUCCESS.getMessage())
                .data(categoryService.getAll())
                .build();
    }

    // POST /v1/ingredient-categories

    @PostMapping
    public ApiResponse<IngredientCategoryResponse> create(@Valid @RequestBody IngredientCategoryRequest request) {
        return ApiResponse.<IngredientCategoryResponse>builder()
                .code(IngredientResponseCode.CATEGORY_CREATE_SUCCESS.getCode())
                .message(IngredientResponseCode.CATEGORY_CREATE_SUCCESS.getMessage())
                .data(categoryService.create(request))
                .build();
    }

    // PUT /v1/ingredient-categories/{id}

    @PutMapping("/{id}")
    public ApiResponse<IngredientCategoryResponse> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody IngredientCategoryRequest request) {
        return ApiResponse.<IngredientCategoryResponse>builder()
                .code(IngredientResponseCode.CATEGORY_UPDATE_SUCCESS.getCode())
                .message(IngredientResponseCode.CATEGORY_UPDATE_SUCCESS.getMessage())
                .data(categoryService.update(id, request))
                .build();
    }

    // DELETE /v1/ingredient-categories/{id}

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") UUID id) {
        categoryService.delete(id);
        return ApiResponse.<Void>builder()
                .code(IngredientResponseCode.CATEGORY_DELETE_SUCCESS.getCode())
                .message(IngredientResponseCode.CATEGORY_DELETE_SUCCESS.getMessage())
                .build();
    }
}
