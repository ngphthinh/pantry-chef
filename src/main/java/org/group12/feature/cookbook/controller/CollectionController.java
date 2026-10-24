package org.group12.feature.cookbook.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.cookbook.dto.request.CollectionCreateRequest;
import org.group12.feature.cookbook.dto.request.CollectionUpdateRequest;
import org.group12.feature.cookbook.dto.response.CollectionResponse;
import org.group12.feature.cookbook.enums.CookbookResponseCode;
import org.group12.feature.cookbook.service.CollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/collections")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionService service;

    @GetMapping
    public ApiResponse<List<CollectionResponse>> list() {
        return ApiResponse.<List<CollectionResponse>>builder()
                .code(CookbookResponseCode.LIST_COLLECTIONS_SUCCESS.getCode())
                .message(CookbookResponseCode.LIST_COLLECTIONS_SUCCESS.getMessage())
                .data(service.list())
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CollectionResponse> create(@Valid @RequestBody CollectionCreateRequest request) {
        return ApiResponse.<CollectionResponse>builder()
                .code(CookbookResponseCode.CREATE_COLLECTION_SUCCESS.getCode())
                .message(CookbookResponseCode.CREATE_COLLECTION_SUCCESS.getMessage())
                .data(service.create(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CollectionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CollectionUpdateRequest request) {
        return ApiResponse.<CollectionResponse>builder()
                .code(CookbookResponseCode.UPDATE_COLLECTION_SUCCESS.getCode())
                .message(CookbookResponseCode.UPDATE_COLLECTION_SUCCESS.getMessage())
                .data(service.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PostMapping("/{id}/recipes/{savedRecipeId}")
    public ApiResponse<CollectionResponse> addRecipe(
            @PathVariable UUID id,
            @PathVariable UUID savedRecipeId) {
        return ApiResponse.<CollectionResponse>builder()
                .code(CookbookResponseCode.ADD_RECIPE_TO_COLLECTION_SUCCESS.getCode())
                .message(CookbookResponseCode.ADD_RECIPE_TO_COLLECTION_SUCCESS.getMessage())
                .data(service.addRecipe(id, savedRecipeId))
                .build();
    }

    @DeleteMapping("/{id}/recipes/{savedRecipeId}")
    public ApiResponse<CollectionResponse> removeRecipe(
            @PathVariable UUID id,
            @PathVariable UUID savedRecipeId) {
        return ApiResponse.<CollectionResponse>builder()
                .code(CookbookResponseCode.REMOVE_RECIPE_FROM_COLLECTION_SUCCESS.getCode())
                .message(CookbookResponseCode.REMOVE_RECIPE_FROM_COLLECTION_SUCCESS.getMessage())
                .data(service.removeRecipe(id, savedRecipeId))
                .build();
    }
}
