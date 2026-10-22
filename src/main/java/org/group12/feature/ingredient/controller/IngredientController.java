package org.group12.feature.ingredient.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.CursorResponse;
import org.group12.feature.ingredient.dto.request.IngredientCreateRequest;
import org.group12.feature.ingredient.dto.request.IngredientUpdateRequest;
import org.group12.feature.ingredient.dto.response.IngredientResponse;
import org.group12.feature.ingredient.enums.IngredientResponseCode;
import org.group12.feature.ingredient.service.IngredientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    // GET /v1/ingredients/search?q=&categoryId=&cursor=&size=
    @GetMapping("/search")
    public ApiResponse<CursorResponse<IngredientResponse>> search(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoryId", required = false) UUID categoryId,
            @RequestParam(name = "cursor", required = false) UUID cursor,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        return ApiResponse.<CursorResponse<IngredientResponse>>builder()
                .code(IngredientResponseCode.INGREDIENT_SEARCH_SUCCESS.getCode())
                .message(IngredientResponseCode.INGREDIENT_SEARCH_SUCCESS.getMessage())
                .data(ingredientService.search(q, categoryId, cursor, size))
                .build();
    }

    // GET /v1/ingredients/barcode/{code}
    @GetMapping("/barcode/{code}")
    public ApiResponse<IngredientResponse> getByBarcode(@PathVariable("code") String code) {
        return ApiResponse.<IngredientResponse>builder()
                .code(IngredientResponseCode.INGREDIENT_BARCODE_SUCCESS.getCode())
                .message(IngredientResponseCode.INGREDIENT_BARCODE_SUCCESS.getMessage())
                .data(ingredientService.getByBarcode(code))
                .build();
    }

    // GET /v1/ingredients/{id}
    @GetMapping("/{id}")
    public ApiResponse<IngredientResponse> getById(@PathVariable("id") UUID id) {
        return ApiResponse.<IngredientResponse>builder()
                .code(IngredientResponseCode.INGREDIENT_GET_SUCCESS.getCode())
                .message(IngredientResponseCode.INGREDIENT_GET_SUCCESS.getMessage())
                .data(ingredientService.getById(id))
                .build();
    }

    // POST /v1/ingredients
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<IngredientResponse> create(@Valid @RequestBody IngredientCreateRequest request) {
        return ApiResponse.<IngredientResponse>builder()
                .code(IngredientResponseCode.INGREDIENT_CREATE_SUCCESS.getCode())
                .message(IngredientResponseCode.INGREDIENT_CREATE_SUCCESS.getMessage())
                .data(ingredientService.create(request))
                .build();
    }

    // PUT /v1/ingredients/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<IngredientResponse> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody IngredientUpdateRequest request) {
        return ApiResponse.<IngredientResponse>builder()
                .code(IngredientResponseCode.INGREDIENT_UPDATE_SUCCESS.getCode())
                .message(IngredientResponseCode.INGREDIENT_UPDATE_SUCCESS.getMessage())
                .data(ingredientService.update(id, request))
                .build();
    }

    // DELETE /v1/ingredients/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") UUID id) {
        ingredientService.delete(id);
        return ApiResponse.<Void>builder()
                .code(IngredientResponseCode.INGREDIENT_DELETE_SUCCESS.getCode())
                .message(IngredientResponseCode.INGREDIENT_DELETE_SUCCESS.getMessage())
                .build();
    }
}
