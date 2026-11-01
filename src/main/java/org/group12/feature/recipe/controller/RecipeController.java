package org.group12.feature.recipe.controller;

import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.CursorResponse;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.dto.response.RecipeSummaryResponse;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.enums.RecipeResponseCode;
import org.group12.feature.recipe.service.RecipeService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Tìm và xem công thức từ API bên ngoài. {@code cursor} ở đây là offset dạng chuỗi
 * (lấy từ {@code nextCursor} của lần gọi trước).
 */
@RestController
@RequestMapping("/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    // GET /v1/recipes/search?ingredients=&cursor=&size=&cuisineId=&maxMinutes=&difficulty=
    @GetMapping("/search")
    public ApiResponse<CursorResponse<RecipeSummaryResponse>> search(
            @RequestParam(required = false) List<String> ingredients,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID cuisineId,
            @RequestParam(required = false) Integer maxMinutes,
            @RequestParam(required = false) Difficulty difficulty) {
        return ApiResponse.<CursorResponse<RecipeSummaryResponse>>builder()
                .code(RecipeResponseCode.RECIPES_FETCHED.getCode())
                .message(RecipeResponseCode.RECIPES_FETCHED.getMessage())
                .data(recipeService.searchByIngredients(ingredients, cursor, size, cuisineId, maxMinutes, difficulty))
                .build();
    }

    // GET /v1/recipes/all?page=0&size=20
    @GetMapping("/all")
    public ApiResponse<Page<RecipeSummaryResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<Page<RecipeSummaryResponse>>builder()
                .code(RecipeResponseCode.RECIPES_FETCHED.getCode())
                .message(RecipeResponseCode.RECIPES_FETCHED.getMessage())
                .data(recipeService.getAllRecipes(page, size))
                .build();
    }

    // GET /v1/recipes/suggestions?cursor=&size=
    @GetMapping("/suggestions")
    public ApiResponse<CursorResponse<RecipeSummaryResponse>> suggestions(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<CursorResponse<RecipeSummaryResponse>>builder()
                .code(RecipeResponseCode.RECIPES_FETCHED.getCode())
                .message(RecipeResponseCode.RECIPES_FETCHED.getMessage())
                .data(recipeService.getSuggestions(cursor, size))
                .build();
    }

    // GET /v1/recipes/by-name?q=&cursor=&size=
    @GetMapping("/by-name")
    public ApiResponse<CursorResponse<RecipeSummaryResponse>> searchByName(
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<CursorResponse<RecipeSummaryResponse>>builder()
                .code(RecipeResponseCode.RECIPES_FETCHED.getCode())
                .message(RecipeResponseCode.RECIPES_FETCHED.getMessage())
                .data(recipeService.searchByName(keyword, cursor, size))
                .build();
    }

    // GET /v1/recipes/random?count=
    @GetMapping("/random")
    public ApiResponse<List<RecipeSummaryResponse>> random(@RequestParam(defaultValue = "5") int count) {
        return ApiResponse.<List<RecipeSummaryResponse>>builder()
                .code(RecipeResponseCode.RECIPES_FETCHED.getCode())
                .message(RecipeResponseCode.RECIPES_FETCHED.getMessage())
                .data(recipeService.getRandom(count))
                .build();
    }

    // GET /v1/recipes/{externalId}
    @GetMapping("/{externalId}")
    public ApiResponse<RecipeDetailResponse> getDetail(@PathVariable String externalId) {
        return ApiResponse.<RecipeDetailResponse>builder()
                .code(RecipeResponseCode.RECIPE_FETCHED.getCode())
                .message(RecipeResponseCode.RECIPE_FETCHED.getMessage())
                .data(recipeService.getByExternalId(externalId))
                .build();
    }

    // GET /v1/recipes/{externalId}/similar
    @GetMapping("/{externalId}/similar")
    public ApiResponse<List<RecipeSummaryResponse>> similar(@PathVariable String externalId) {
        return ApiResponse.<List<RecipeSummaryResponse>>builder()
                .code(RecipeResponseCode.RECIPES_FETCHED.getCode())
                .message(RecipeResponseCode.RECIPES_FETCHED.getMessage())
                .data(recipeService.getSimilar(externalId))
                .build();
    }

    // GET /v1/recipes/{externalId}/scale?servings=
    @GetMapping("/{externalId}/scale")
    public ApiResponse<RecipeDetailResponse> scale(@PathVariable String externalId,
                                                   @RequestParam int servings) {
        return ApiResponse.<RecipeDetailResponse>builder()
                .code(RecipeResponseCode.RECIPE_SCALED.getCode())
                .message(RecipeResponseCode.RECIPE_SCALED.getMessage())
                .data(recipeService.scale(externalId, servings))
                .build();
    }
}
