package org.group12.feature.cookbook.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.CursorResponse;
import org.group12.feature.cookbook.dto.request.SavedRecipeCreateRequest;
import org.group12.feature.cookbook.dto.request.SavedRecipeUpdateRequest;
import org.group12.feature.cookbook.dto.response.SavedRecipeDetailResponse;
import org.group12.feature.cookbook.dto.response.SavedRecipeResponse;
import org.group12.feature.cookbook.enums.CookbookResponseCode;
import org.group12.feature.cookbook.service.SavedRecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/saved-recipes")
@RequiredArgsConstructor
@Validated
public class SavedRecipeController {
    private final SavedRecipeService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SavedRecipeResponse> create(
            @Valid @RequestBody SavedRecipeCreateRequest request) {
        return ApiResponse.<SavedRecipeResponse>builder()
                        .code(CookbookResponseCode.SAVE_RECIPE_SUCCESS.getCode())
                        .message(CookbookResponseCode.SAVE_RECIPE_SUCCESS.getMessage())
                        .data(service.save(request))
                        .build();
    }

    @GetMapping
    public ApiResponse<CursorResponse<SavedRecipeResponse>> list(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) UUID collectionId) {
        return ApiResponse.<CursorResponse<SavedRecipeResponse>>builder()
                .code(CookbookResponseCode.LIST_SAVED_RECIPES_SUCCESS.getCode())
                .message(CookbookResponseCode.LIST_SAVED_RECIPES_SUCCESS.getMessage())
                .data(service.list(cursor, size, favorite, collectionId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SavedRecipeDetailResponse> getDetail(@PathVariable UUID id) {
        return ApiResponse.<SavedRecipeDetailResponse>builder()
                .code(CookbookResponseCode.GET_SAVED_RECIPE_SUCCESS.getCode())
                .message(CookbookResponseCode.GET_SAVED_RECIPE_SUCCESS.getMessage())
                .data(service.getDetail(id))
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PatchMapping("/{id}")
    public ApiResponse<SavedRecipeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SavedRecipeUpdateRequest request) {
        return ApiResponse.<SavedRecipeResponse>builder()
                .code(CookbookResponseCode.UPDATE_SAVED_RECIPE_SUCCESS.getCode())
                .message(CookbookResponseCode.UPDATE_SAVED_RECIPE_SUCCESS.getMessage())
                .data(service.update(id, request))
                .build();
    }
}
