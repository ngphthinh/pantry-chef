package org.group12.feature.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.ai.dto.request.AiMealPlanRequest;
import org.group12.feature.ai.dto.request.ImageUploadRequest;
import org.group12.feature.ai.dto.request.SubstitutionRequest;
import org.group12.feature.ai.dto.response.MealPlanResponse;
import org.group12.feature.ai.dto.response.RecognizedIngredientResponse;
import org.group12.feature.ai.dto.response.SubstitutionResponse;
import org.group12.feature.ai.enums.AiResponseCode;
import org.group12.feature.ai.service.AiMealPlanService;
import org.group12.feature.ai.service.IngredientRecognitionService;
import org.group12.feature.ai.service.SubstitutionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final IngredientRecognitionService service;
    private final SubstitutionService substitutionService;
    private final AiMealPlanService aiMealPlanService;

    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<RecognizedIngredientResponse>> recognize(
            @Valid @ModelAttribute ImageUploadRequest request) {
        return ApiResponse.<List<RecognizedIngredientResponse>>builder()
                .code(AiResponseCode.AI_RECOGNIZE_INGREDIENT_SUCCESS.getCode())
                .message(AiResponseCode.AI_RECOGNIZE_INGREDIENT_SUCCESS.getMessage())
                .data(service.recognize(request))
                .build();
    }

    @PostMapping("/substitutions")
    public ApiResponse<List<SubstitutionResponse>> substitutions(@Valid @RequestBody SubstitutionRequest request) {
        return ApiResponse.<List<SubstitutionResponse>>builder()
                .code(AiResponseCode.AI_SUBSTITUTIONS_SUCCESS.getCode())
                .message(AiResponseCode.AI_SUBSTITUTIONS_SUCCESS.getMessage())
                .data(substitutionService.suggest(request))
                .build();
    }

    @PostMapping("/meal-plan")
    public ApiResponse<MealPlanResponse> mealPlan(@Valid @RequestBody AiMealPlanRequest request) {
        return ApiResponse.<MealPlanResponse>builder()
                .code(AiResponseCode.AI_MEAL_PLAN_SUCCESS.getCode())
                .message(AiResponseCode.AI_MEAL_PLAN_SUCCESS.getMessage())
                .data(aiMealPlanService.generate(request))
                .build();
    }
}