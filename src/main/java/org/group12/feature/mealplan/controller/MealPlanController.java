package org.group12.feature.mealplan.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.mealplan.dto.request.MealPlanCreateRequest;
import org.group12.feature.mealplan.dto.request.MealPlanEntryCreateRequest;
import org.group12.feature.mealplan.dto.request.MealPlanEntryUpdateRequest;
import org.group12.feature.mealplan.dto.response.MealPlanEntryResponse;
import org.group12.feature.mealplan.dto.response.MealPlanResponse;
import org.group12.feature.mealplan.enums.MealPlanResponseCode;
import org.group12.feature.mealplan.service.MealPlanService;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/meal-plans")
@RequiredArgsConstructor

public class MealPlanController {

    private final MealPlanService mealPlanService;

    // GET /v1/meal-plans?weekStart=
    @GetMapping
    public ApiResponse<MealPlanResponse> getByWeekStart(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        return ApiResponse.<MealPlanResponse>builder()
                .code(MealPlanResponseCode.MEAL_PLAN_FETCHED.getCode())
                .message(MealPlanResponseCode.MEAL_PLAN_FETCHED.getMessage())
                .data(mealPlanService.getByWeekStart(weekStart))
                .build();
    }

    // GET /v1/meal-plans/{id}
    @GetMapping("/{id}")
    public ApiResponse<MealPlanResponse> getById(@PathVariable UUID id) {
        return ApiResponse.<MealPlanResponse>builder()
                .code(MealPlanResponseCode.MEAL_PLAN_FETCHED.getCode())
                .message(MealPlanResponseCode.MEAL_PLAN_FETCHED.getMessage())
                .data(mealPlanService.getById(id))
                .build();
    }

    // POST /v1/meal-plans
    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<MealPlanResponse> create(@Valid @RequestBody MealPlanCreateRequest request) {
        return ApiResponse.<MealPlanResponse>builder()
                .code(MealPlanResponseCode.MEAL_PLAN_CREATED.getCode())
                .message(MealPlanResponseCode.MEAL_PLAN_CREATED.getMessage())
                .data(mealPlanService.create(request))
                .build();
    }

    // DELETE /v1/meal-plans/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        mealPlanService.delete(id);
    }

    // POST /v1/meal-plans/{id}/entries
    @PostMapping("/{id}/entries")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<MealPlanEntryResponse> addEntry(
            @PathVariable UUID id,
            @Valid @RequestBody MealPlanEntryCreateRequest request
    ) {
        return ApiResponse.<MealPlanEntryResponse>builder()
                .code(MealPlanResponseCode.MEAL_PLAN_ENTRY_CREATED.getCode())
                .message(MealPlanResponseCode.MEAL_PLAN_ENTRY_CREATED.getMessage())
                .data(mealPlanService.addEntry(id, request))
                .build();
    }

    // PATCH /v1/meal-plans/{id}/entries/{entryId}
    @PatchMapping("/{id}/entries/{entryId}")
    public ApiResponse<MealPlanEntryResponse> updateEntry(
            @PathVariable UUID id,
            @PathVariable UUID entryId,
            @Valid @RequestBody MealPlanEntryUpdateRequest request
    ) {
        return ApiResponse.<MealPlanEntryResponse>builder()
                .code(MealPlanResponseCode.MEAL_PLAN_ENTRY_UPDATED.getCode())
                .message(MealPlanResponseCode.MEAL_PLAN_ENTRY_UPDATED.getMessage())
                .data(mealPlanService.updateEntry(id, entryId, request))
                .build();
    }

    // DELETE /v1/meal-plans/{id}/entries/{entryId}
    @DeleteMapping("/{id}/entries/{entryId}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteEntry(
            @PathVariable UUID id,
            @PathVariable UUID entryId
    ) {
        mealPlanService.deleteEntry(id, entryId);
    }

    // POST /v1/meal-plans/{id}/copy?toWeekStart=
    @PostMapping("/{id}/copy")
    public ApiResponse<MealPlanResponse> copy(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toWeekStart
    ) {
        return ApiResponse.<MealPlanResponse>builder()
                .code(MealPlanResponseCode.MEAL_PLAN_COPIED.getCode())
                .message(MealPlanResponseCode.MEAL_PLAN_COPIED.getMessage())
                .data(mealPlanService.copy(id, toWeekStart))
                .build();
    }

    // GET /v1/meal-plans/{id}/shopping-list
    @GetMapping("/{id}/shopping-list")
    public ApiResponse<List<ShoppingItemResponse>> getShoppingListPreview(@PathVariable UUID id) {
        return ApiResponse.<List<ShoppingItemResponse>>builder()
                .code(MealPlanResponseCode.SHOPPING_LIST_PREVIEW_FETCHED.getCode())
                .message(MealPlanResponseCode.SHOPPING_LIST_PREVIEW_FETCHED.getMessage())
                .data(mealPlanService.getShoppingListPreview(id))
                .build();
    }
}
