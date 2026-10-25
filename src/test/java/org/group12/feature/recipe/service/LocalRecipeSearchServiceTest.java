package org.group12.feature.recipe.service;

import org.group12.feature.recipe.dto.response.RecipeSummaryResponse;
import org.group12.feature.recipe.entity.Cuisine;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.mapper.RecipeMapper;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.group12.feature.recipe.repository.RecipeRepository.LocalIngredientRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalRecipeSearchServiceTest {

    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private RecipeMapper recipeMapper;

    @InjectMocks
    private LocalRecipeSearchService localRecipeSearchService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(recipeMapper.toSummaryResponse(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe recipe = invocation.getArgument(0);
            return RecipeSummaryResponse.builder().id(recipe.getId()).title(recipe.getTitle()).build();
        });
    }

    private LocalIngredientRow row(UUID recipeId, String name) {
        return new LocalIngredientRow() {
            @Override
            public UUID getRecipeId() {
                return recipeId;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    private Recipe recipe(UUID id, String title) {
        return Recipe.builder().id(id).title(title).build();
    }

    @Test
    void searchByIngredients_ranksByUsedThenFewestMissing() {
        UUID r1 = UUID.randomUUID(), r2 = UUID.randomUUID(), r3 = UUID.randomUUID();
        when(recipeRepository.findAccessibleIngredientRows(userId)).thenReturn(List.of(
                row(r1, "Thịt bò"), row(r1, "Hành lá"),
                row(r2, "Thịt bò"), row(r2, "Cà chua"), row(r2, "Trứng"),
                row(r3, "Cá lóc")));
        when(recipeRepository.findByIdIn(anyCollection())).thenReturn(List.of(
                recipe(r2, "Bò xào cà"), recipe(r1, "Bò hành")));

        List<RecipeSummaryResponse> result = localRecipeSearchService.searchByIngredients(
                userId, List.of("thịt bò", "hành"), List.of(), null, null, null);

        assertEquals(List.of("Bò hành", "Bò xào cà"), result.stream().map(RecipeSummaryResponse::getTitle).toList());
        assertEquals(2, result.get(0).getUsedIngredientCount());
        assertEquals(0, result.get(0).getMissedIngredientCount());
        assertEquals(1.0, result.get(0).getMatchScore());
        assertEquals(1, result.get(1).getUsedIngredientCount());
        assertEquals(List.of("Cà chua", "Trứng"), result.get(1).getMissingIngredients());
        assertEquals(1.0 / 3.0, result.get(1).getMatchScore(), 1e-9);

        verify(recipeRepository).findByIdIn(argThat(ids -> ids.size() == 2 && ids.containsAll(List.of(r1, r2))));
    }

    @Test
    void searchByIngredients_skipsRecipesWithExcludedIngredients() {
        UUID r1 = UUID.randomUUID();
        when(recipeRepository.findAccessibleIngredientRows(userId)).thenReturn(List.of(
                row(r1, "Trứng"), row(r1, "Tôm khô")));

        List<RecipeSummaryResponse> result = localRecipeSearchService.searchByIngredients(
                userId, List.of("trứng"), List.of("tôm"), null, null, null);

        assertTrue(result.isEmpty());
        verify(recipeRepository, never()).findByIdIn(anyCollection());
    }

    @Test
    void searchByIngredients_appliesCuisineTimeAndDifficultyFilters() {
        UUID r1 = UUID.randomUUID(), r2 = UUID.randomUUID();
        UUID cuisineId = UUID.randomUUID();
        Cuisine asian = Cuisine.builder().id(cuisineId).code("ASIAN").name("Asian").build();
        when(recipeRepository.findAccessibleIngredientRows(userId)).thenReturn(List.of(row(r1, "Trứng"), row(r2, "Trứng")));
        when(recipeRepository.findByIdIn(anyCollection())).thenReturn(List.of(
                Recipe.builder().id(r1).title("Nhanh").cuisine(asian).readyInMinutes(20).difficulty(Difficulty.EASY).build(),
                Recipe.builder().id(r2).title("Lâu").cuisine(asian).readyInMinutes(120).difficulty(Difficulty.HARD).build()));

        List<RecipeSummaryResponse> result = localRecipeSearchService.searchByIngredients(
                userId, List.of("trứng"), List.of(), cuisineId, 30, Difficulty.EASY);

        assertEquals(List.of("Nhanh"), result.stream().map(RecipeSummaryResponse::getTitle).toList());
    }

    @Test
    void searchByIngredients_noIngredients_returnsEmptyWithoutQuery() {
        assertTrue(localRecipeSearchService.searchByIngredients(
                userId, List.of(), List.of(), null, null, null).isEmpty());
        verify(recipeRepository, never()).findAccessibleIngredientRows(any());
    }

    @Test
    void searchByName_buildsLowercaseEscapedPattern() {
        when(recipeRepository.findAccessibleByTitle(eq(userId), any(String.class), any(Pageable.class))).thenReturn(List.of());

        localRecipeSearchService.searchByName(userId, " 100%_Mì ", List.of());

        ArgumentCaptor<String> pattern = ArgumentCaptor.forClass(String.class);
        verify(recipeRepository).findAccessibleByTitle(eq(userId), pattern.capture(), any(Pageable.class));
        assertEquals("%100\\%\\_mì%", pattern.getValue());
    }

    @Test
    void searchByName_dropsRecipesContainingExcludedIngredients() {
        UUID r1 = UUID.randomUUID(), r2 = UUID.randomUUID();
        when(recipeRepository.findAccessibleByTitle(eq(userId), any(String.class), any(Pageable.class)))
                .thenReturn(List.of(recipe(r1, "Mì hoành thánh"), recipe(r2, "Mì xào")));
        when(recipeRepository.findIngredientRowsByRecipeIds(anyCollection())).thenReturn(List.of(
                row(r1, "Tôm"), row(r1, "Mì trứng"), row(r2, "Mì trứng")));

        List<RecipeSummaryResponse> result = localRecipeSearchService.searchByName(userId, "mì", List.of("tôm"));

        assertEquals(List.of("Mì xào"), result.stream().map(RecipeSummaryResponse::getTitle).toList());
    }

    @Test
    void searchByName_blankKeyword_returnsEmptyWithoutQuery() {
        assertTrue(localRecipeSearchService.searchByName(userId, "  ", List.of()).isEmpty());
        verify(recipeRepository, never()).findAccessibleByTitle(any(), any(), any());
    }
}
