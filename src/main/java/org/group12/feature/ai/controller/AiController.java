package org.group12.feature.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.ai.dto.request.*;
import org.group12.feature.ai.dto.response.*;
import org.group12.feature.ai.enums.AiResponseCode;
import org.group12.feature.ai.service.*;
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
    private final AiRecipeIdeaService aiRecipeIdeaService;
    private final AdaptRecipeService adaptRecipeService;

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


    @PostMapping("/recipe-ideas")
    public ApiResponse<List<RecipeIdeaResponse>> recipeIdeas(@Valid @RequestBody RecipeIdeasRequest request) {
        return ApiResponse.<List<RecipeIdeaResponse>>builder()
                .code(AiResponseCode.AI_RECIPE_IDEAS_SUCCESS.getCode())
                .message(AiResponseCode.AI_RECIPE_IDEAS_SUCCESS.getMessage())
                .data(aiRecipeIdeaService.suggest(request))
                .build();
    }

    @PostMapping("/adapt-recipe")
    public ApiResponse<RecipeDetailResponse> adaptRecipe(@Valid @RequestBody AdaptRecipeRequest request) {
        return ApiResponse.<RecipeDetailResponse>builder()
                .code(AiResponseCode.ADAPT_RECIPE_SUCCESS.getCode())
                .message(AiResponseCode.ADAPT_RECIPE_SUCCESS.getMessage())
                .data(adaptRecipeService.adapt(request))
                .build();
    }

}