package org.group12.feature.recipe.controller;

import org.group12.common.dto.response.CursorResponse;
import org.group12.common.exception.GlobalExceptionHandler;
import org.group12.common.utils.AppUtil;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.dto.response.RecipeSummaryResponse;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.exception.InvalidRecipeCursorException;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.exception.RecipeProviderQuotaExceededException;
import org.group12.feature.recipe.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecipeControllerTest {

    private final RecipeService recipeService = mock(RecipeService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RecipeController(recipeService))
                .setControllerAdvice(new GlobalExceptionHandler(new AppUtil()))
                .build();
    }

    @Test
    void search_passesQueryParametersToService() throws Exception {
        CursorResponse<RecipeSummaryResponse> page = CursorResponse.<RecipeSummaryResponse>builder()
                .content(List.of(RecipeSummaryResponse.builder().externalId("1").title("Pho").build()))
                .hasNext(true).nextCursor("5").build();
        when(recipeService.searchByIngredients(any(), any(), eq(5), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/v1/recipes/search")
                        .param("ingredients", "egg,tomato")
                        .param("size", "5")
                        .param("maxMinutes", "30")
                        .param("difficulty", "EASY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("RECIPE_200"))
                .andExpect(jsonPath("$.data.content[0].externalId").value("1"))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value("5"));

        verify(recipeService).searchByIngredients(
                eq(List.of("egg", "tomato")), isNull(), eq(5), isNull(), eq(30), eq(Difficulty.EASY));
    }

    @Test
    void search_invalidCursor_returnsBadRequest() throws Exception {
        when(recipeService.searchByIngredients(any(), any(), eq(20), any(), any(), any()))
                .thenThrow(new InvalidRecipeCursorException());

        mockMvc.perform(get("/v1/recipes/search").param("ingredients", "egg").param("cursor", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_invalidDifficulty_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/v1/recipes/search").param("ingredients", "egg").param("difficulty", "EXTREME"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDetail_returnsRecipe() throws Exception {
        when(recipeService.getByExternalId("716429"))
                .thenReturn(RecipeDetailResponse.builder().externalId("716429").title("Pasta").build());

        mockMvc.perform(get("/v1/recipes/716429"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.externalId").value("716429"))
                .andExpect(jsonPath("$.data.title").value("Pasta"));
    }

    @Test
    void getDetail_notFound_returns404() throws Exception {
        when(recipeService.getByExternalId("999")).thenThrow(new RecipeNotFoundException());

        mockMvc.perform(get("/v1/recipes/999")).andExpect(status().isNotFound());
    }

    @Test
    void scale_providerQuotaExceeded_returns429() throws Exception {
        when(recipeService.scale("123", 4)).thenThrow(new RecipeProviderQuotaExceededException());

        mockMvc.perform(get("/v1/recipes/123/scale").param("servings", "4"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void scale_servingsNotANumber_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/v1/recipes/123/scale").param("servings", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void random_usesDefaultCount() throws Exception {
        when(recipeService.getRandom(5)).thenReturn(List.of(RecipeSummaryResponse.builder().externalId("7").build()));

        mockMvc.perform(get("/v1/recipes/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].externalId").value("7"));
    }
}
