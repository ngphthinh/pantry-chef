package org.group12.feature.recipe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.recipe.dto.request.CuisineRequest;
import org.group12.feature.recipe.dto.response.CuisineResponse;
import org.group12.feature.recipe.enums.CuisineResponseCode;
import org.group12.feature.recipe.service.CuisineService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/cuisines")
@RequiredArgsConstructor
public class CuisineController {

    private final CuisineService cuisineService;

    // GET /v1/cuisines  (công khai)
    @GetMapping
    public ApiResponse<List<CuisineResponse>> list() {
        return ApiResponse.<List<CuisineResponse>>builder()
                .code(CuisineResponseCode.CUISINES_FETCHED.getCode())
                .message(CuisineResponseCode.CUISINES_FETCHED.getMessage())
                .data(cuisineService.getAll())
                .build();
    }

    // POST /v1/cuisines  (ADMIN)
    @PostMapping
    @ResponseStatus(code = org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<CuisineResponse> create(@Valid @RequestBody CuisineRequest request) {
        return ApiResponse.<CuisineResponse>builder()
                .code(CuisineResponseCode.CUISINE_CREATED.getCode())
                .message(CuisineResponseCode.CUISINE_CREATED.getMessage())
                .data(cuisineService.create(request))
                .build();
    }

    // PUT /v1/cuisines/{id}  (ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<CuisineResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody CuisineRequest request) {
        return ApiResponse.<CuisineResponse>builder()
                .code(CuisineResponseCode.CUISINE_UPDATED.getCode())
                .message(CuisineResponseCode.CUISINE_UPDATED.getMessage())
                .data(cuisineService.update(id, request))
                .build();
    }

    // DELETE /v1/cuisines/{id}  (ADMIN)
    @DeleteMapping("/{id}")
    @ResponseStatus(code = org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        cuisineService.delete(id);
    }
}
