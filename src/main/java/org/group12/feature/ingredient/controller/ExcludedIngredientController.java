package org.group12.feature.ingredient.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.ingredient.dto.request.ExcludedIngredientCreateRequest;
import org.group12.feature.ingredient.dto.response.ExcludedIngredientResponse;
import org.group12.feature.ingredient.enums.IngredientResponseCode;
import org.group12.feature.ingredient.service.ExcludedIngredientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users/me/excluded-ingredients")
@RequiredArgsConstructor
public class ExcludedIngredientController {

    private final ExcludedIngredientService excludedIngredientService;

    // GET /v1/users/me/excluded-ingredients
    @GetMapping
    public ApiResponse<List<ExcludedIngredientResponse>> getMyExcludedIngredients() {
        return ApiResponse.<List<ExcludedIngredientResponse>>builder()
                .code(IngredientResponseCode.EXCLUDED_LIST_SUCCESS.getCode())
                .message(IngredientResponseCode.EXCLUDED_LIST_SUCCESS.getMessage())
                .data(excludedIngredientService.getMyExcludedIngredients())
                .build();
    }

    // POST /v1/users/me/excluded-ingredients
    @PostMapping
    public ApiResponse<ExcludedIngredientResponse> addExcludedIngredient(@Valid @RequestBody ExcludedIngredientCreateRequest request) {
        return ApiResponse.<ExcludedIngredientResponse>builder()
                .code(IngredientResponseCode.EXCLUDED_CREATE_SUCCESS.getCode())
                .message(IngredientResponseCode.EXCLUDED_CREATE_SUCCESS.getMessage())
                .data(excludedIngredientService.addExcludedIngredient(request))
                .build();
    }

    // DELETE /v1/users/me/excluded-ingredients/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void removeExcludedIngredient(@PathVariable("id") UUID id) {
        excludedIngredientService.removeExcludedIngredient(id);
    }
}
