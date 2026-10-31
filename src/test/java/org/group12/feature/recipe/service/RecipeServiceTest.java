package org.group12.feature.recipe.service;

import org.group12.common.dto.response.CursorResponse;
import org.group12.common.utils.AppUtil;
import org.group12.feature.ingredient.entity.ExcludedIngredient;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.ingredient.repository.ExcludedIngredientRepository;
import org.group12.feature.pantry.entity.PantryItem;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.group12.feature.recipe.client.RecipeProviderClient;
import org.group12.feature.recipe.client.model.ExternalRecipeDetail;
import org.group12.feature.recipe.client.model.ExternalRecipePage;
import org.group12.feature.recipe.client.model.ExternalRecipeSummary;
import org.group12.feature.recipe.client.model.RecipeSearchCriteria;
import org.group12.feature.recipe.client.model.RecipeSort;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.dto.response.RecipeIngredientResponse;
import org.group12.feature.recipe.dto.response.RecipeSummaryResponse;
import org.group12.feature.recipe.entity.Cuisine;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.exception.InvalidRecipeCursorException;
import org.group12.feature.recipe.exception.InvalidRecipeSearchException;
import org.group12.feature.recipe.exception.InvalidServingsException;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.exception.RecipeProviderException;
import org.group12.feature.recipe.mapper.CuisineMapper;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeProviderClient recipeProviderClient;
    @Mock
    private RecipeImportService recipeImportService;
    @Mock
    private LocalRecipeSearchService localRecipeSearchService;
    @Mock
    private CuisineRepository cuisineRepository;
    @Mock
    private CuisineMapper cuisineMapper;
    @Mock
    private PantryItemRepository pantryItemRepository;
    @Mock
    private ExcludedIngredientRepository excludedIngredientRepository;
    @Mock
    private AppUtil appUtil;

    @InjectMocks
    private RecipeService recipeService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    private void authenticate() {
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
    }

    private ExternalRecipeSummary summary(String id, int readyMinutes, Integer used, Integer missed) {
        return new ExternalRecipeSummary(id, "Recipe " + id, "http://img/" + id, readyMinutes, 2, null,
                used, missed, missed == null ? List.of() : List.of("salt"));
    }

    // ---------- searchByIngredients ----------

    @Test
    void searchByIngredients_normalizesIngredientsAndBuildsCursor() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of(
                ExcludedIngredient.builder().id(UUID.randomUUID()).name(" Shrimp ").build()));
        when(recipeProviderClient.search(any(), eq(0), eq(2)))
                .thenReturn(new ExternalRecipePage(List.of(summary("1", 20, 2, 1), summary("2", 50, 1, 3)), 10));

        CursorResponse<RecipeSummaryResponse> result = recipeService.searchByIngredients(
                List.of(" Egg ", "tomato", "egg", " "), null, 2, null, null, null);

        ArgumentCaptor<RecipeSearchCriteria> captor = ArgumentCaptor.forClass(RecipeSearchCriteria.class);
        verify(recipeProviderClient).search(captor.capture(), eq(0), eq(2));
        assertEquals(List.of("egg", "tomato"), captor.getValue().includeIngredients());
        assertEquals(List.of("shrimp"), captor.getValue().excludeIngredients());
        assertEquals(RecipeSort.MIN_MISSING_INGREDIENTS, captor.getValue().sort());

        assertEquals(2, result.getContent().size());
        assertTrue(result.isHasNext());
        assertEquals("2", result.getNextCursor());
        assertEquals(2.0 / 3.0, result.getContent().get(0).getMatchScore(), 1e-9);
        assertEquals(Difficulty.EASY, result.getContent().get(0).getDifficulty());
        assertEquals(Difficulty.MEDIUM, result.getContent().get(1).getDifficulty());
    }

    @Test
    void searchByIngredients_lastPage_hasNoNextCursor() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of());
        when(recipeProviderClient.search(any(), eq(4), eq(20)))
                .thenReturn(new ExternalRecipePage(List.of(summary("9", 20, 1, 0)), 5));

        CursorResponse<RecipeSummaryResponse> result =
                recipeService.searchByIngredients(List.of("egg"), "4", 20, null, null, null);

        assertFalse(result.isHasNext());
        assertNull(result.getNextCursor());
    }

    @Test
    void searchByIngredients_difficultyFiltersResultsAndCapsMaxMinutes() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of());
        when(recipeProviderClient.search(any(), eq(0), eq(20)))
                .thenReturn(new ExternalRecipePage(List.of(summary("1", 20, 1, 0), summary("2", 90, 1, 0)), 2));

        CursorResponse<RecipeSummaryResponse> result =
                recipeService.searchByIngredients(List.of("egg"), null, 20, null, 45, Difficulty.EASY);

        ArgumentCaptor<RecipeSearchCriteria> captor = ArgumentCaptor.forClass(RecipeSearchCriteria.class);
        verify(recipeProviderClient).search(captor.capture(), eq(0), eq(20));
        assertEquals(30, captor.getValue().maxReadyMinutes());
        assertEquals(1, result.getContent().size());
        assertEquals("1", result.getContent().get(0).getExternalId());
    }

    @Test
    void searchByIngredients_noIngredients_throwsInvalidSearch() {
        authenticate();

        assertThrows(InvalidRecipeSearchException.class,
                () -> recipeService.searchByIngredients(List.of(" "), null, 20, null, null, null));
        assertThrows(InvalidRecipeSearchException.class,
                () -> recipeService.searchByIngredients(null, null, 20, null, null, null));
        verifyNoInteractions(recipeProviderClient);
    }

    @Test
    void searchByIngredients_invalidCursor_throwsBeforeCallingProvider() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of());

        assertThrows(InvalidRecipeCursorException.class,
                () -> recipeService.searchByIngredients(List.of("egg"), "abc", 20, null, null, null));
        assertThrows(InvalidRecipeCursorException.class,
                () -> recipeService.searchByIngredients(List.of("egg"), "-1", 20, null, null, null));
        verify(recipeProviderClient, never()).search(any(), anyInt(), anyInt());
    }

    @Test
    void searchByIngredients_unsupportedCuisine_returnsEmptyPageWithoutCallingProvider() {
        authenticate();
        UUID cuisineId = UUID.randomUUID();
        when(cuisineRepository.findById(cuisineId))
                .thenReturn(Optional.of(Cuisine.builder().id(cuisineId).code("OTHER").name("Other").build()));
        when(recipeProviderClient.supportsCuisine("Other")).thenReturn(false);

        CursorResponse<RecipeSummaryResponse> result =
                recipeService.searchByIngredients(List.of("egg"), null, 20, cuisineId, null, null);

        assertTrue(result.getContent().isEmpty());
        assertFalse(result.isHasNext());
        verify(recipeProviderClient, never()).search(any(), anyInt(), anyInt());
    }

    // ---------- suggestions ----------

    @Test
    void getSuggestions_usesPantryIngredientsWithoutExcludedOrEmptyOnes() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of(
                ExcludedIngredient.builder().id(UUID.randomUUID()).name("Shrimp").build()));
        when(pantryItemRepository.findByUserIdNotIngredientCategory(userId)).thenReturn(List.of(
                pantry("Egg", 3.0, LocalDate.now().plusDays(5)),
                pantry("Shrimp", 1.0, LocalDate.now().plusDays(1)),
                pantry("Milk", 0.0, LocalDate.now().plusDays(2)),
                pantry("Tomato", 2.0, null)));
        when(recipeProviderClient.search(any(), eq(0), eq(20)))
                .thenReturn(new ExternalRecipePage(List.of(), 0));

        recipeService.getSuggestions(null, 20);

        ArgumentCaptor<RecipeSearchCriteria> captor = ArgumentCaptor.forClass(RecipeSearchCriteria.class);
        verify(recipeProviderClient).search(captor.capture(), eq(0), eq(20));
        assertEquals(List.of("egg", "tomato"), captor.getValue().includeIngredients());
        assertEquals(List.of("shrimp"), captor.getValue().excludeIngredients());
    }

    @Test
    void getSuggestions_emptyPantry_returnsEmptyPage() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of());
        when(pantryItemRepository.findByUserIdNotIngredientCategory(userId)).thenReturn(new ArrayList<>());

        CursorResponse<RecipeSummaryResponse> result = recipeService.getSuggestions(null, 20);

        assertTrue(result.getContent().isEmpty());
        verifyNoInteractions(recipeProviderClient);
    }

    private PantryItem pantry(String ingredientName, Double quantity, LocalDate expiry) {
        return PantryItem.builder()
                .id(UUID.randomUUID())
                .ingredient(Ingredient.builder().id(UUID.randomUUID()).name(ingredientName).build())
                .quantity(quantity)
                .expiryDate(expiry)
                .build();
    }

    // ---------- by name ----------

    @Test
    void searchByName_blankKeyword_throwsInvalidSearch() {
        authenticate();

        assertThrows(InvalidRecipeSearchException.class, () -> recipeService.searchByName("  ", null, 20));
    }

    // ---------- detail ----------

    @Test
    void getByExternalId_returnsLocalCopyWithoutCallingProvider() {
        RecipeDetailResponse local = RecipeDetailResponse.builder().id(UUID.randomUUID()).externalId("123").build();
        when(recipeImportService.findImported("123")).thenReturn(Optional.of(local));

        assertEquals(local, recipeService.getByExternalId("123"));
        verifyNoInteractions(recipeProviderClient);
    }

    @Test
    void getByExternalId_importsFromProviderWhenMissingLocally() {
        ExternalRecipeDetail external = new ExternalRecipeDetail("123", "Pho", null, 2, 30, null, null, List.of(), List.of());
        RecipeDetailResponse imported = RecipeDetailResponse.builder().id(UUID.randomUUID()).externalId("123").build();
        when(recipeImportService.findImported("123")).thenReturn(Optional.empty());
        when(recipeProviderClient.getDetail("123")).thenReturn(external);
        when(recipeImportService.importRecipe(external)).thenReturn(imported);

        assertEquals(imported, recipeService.getByExternalId("123"));
    }

    @Test
    void getByExternalId_concurrentImport_readsTheSavedCopy() {
        ExternalRecipeDetail external = new ExternalRecipeDetail("123", "Pho", null, 2, 30, null, null, List.of(), List.of());
        RecipeDetailResponse saved = RecipeDetailResponse.builder().id(UUID.randomUUID()).externalId("123").build();
        when(recipeImportService.findImported("123")).thenReturn(Optional.empty(), Optional.of(saved));
        when(recipeProviderClient.getDetail("123")).thenReturn(external);
        when(recipeImportService.importRecipe(external)).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertEquals(saved, recipeService.getByExternalId("123"));
    }

    @Test
    void getByExternalId_invalidId_throwsNotFoundWithoutAnyCall() {
        assertThrows(RecipeNotFoundException.class, () -> recipeService.getByExternalId("../admin"));
        assertThrows(RecipeNotFoundException.class, () -> recipeService.getByExternalId("12 3"));
        verifyNoInteractions(recipeImportService, recipeProviderClient);
    }

    // ---------- scale ----------

    @Test
    void scale_multipliesAmountsAndKeepsNulls() {
        RecipeDetailResponse detail = RecipeDetailResponse.builder()
                .externalId("123").servings(2)
                .ingredients(List.of(
                        RecipeIngredientResponse.builder().name("rice").amount(100.0).build(),
                        RecipeIngredientResponse.builder().name("salt").amount(null).build(),
                        RecipeIngredientResponse.builder().name("egg").amount(1.0).build()))
                .build();
        when(recipeImportService.findImported("123")).thenReturn(Optional.of(detail));

        RecipeDetailResponse result = recipeService.scale("123", 3);

        assertEquals(3, result.getServings());
        assertEquals(150.0, result.getIngredients().get(0).getAmount());
        assertNull(result.getIngredients().get(1).getAmount());
        assertEquals(1.5, result.getIngredients().get(2).getAmount());
    }

    @Test
    void scale_invalidServings_throws() {
        assertThrows(InvalidServingsException.class, () -> recipeService.scale("123", 0));
        assertThrows(InvalidServingsException.class, () -> recipeService.scale("123", 1000));
        verifyNoInteractions(recipeImportService, recipeProviderClient);
    }

    @Test
    void scale_recipeWithoutBaseServings_throws() {
        RecipeDetailResponse detail = RecipeDetailResponse.builder().externalId("123").servings(null).ingredients(List.of()).build();
        when(recipeImportService.findImported("123")).thenReturn(Optional.of(detail));

        assertThrows(InvalidServingsException.class, () -> recipeService.scale("123", 2));
    }

    // ---------- tìm gộp DB + API ngoài ----------

    private RecipeSummaryResponse local(String title, String externalId) {
        return RecipeSummaryResponse.builder().id(UUID.randomUUID()).title(title).externalId(externalId).custom(externalId == null).build();
    }

    @Test
    void searchByName_putsLocalFirstAndDropsDuplicatesOfImportedRecipes() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of());
        RecipeSummaryResponse mine = local("Mì hoành thánh", null);
        RecipeSummaryResponse imported = local("Pork Patty Bánh Mì", "5");
        when(localRecipeSearchService.searchByName(eq(userId), eq("mì"), anyCollection())).thenReturn(List.of(mine, imported));
        when(recipeProviderClient.search(any(), eq(0), eq(20))).thenReturn(
                new ExternalRecipePage(List.of(summary("5", 20, null, null), summary("6", 20, null, null)), 40));

        CursorResponse<RecipeSummaryResponse> result = recipeService.searchByName(" mì ", null, 20);

        assertEquals(List.of("Mì hoành thánh", "Pork Patty Bánh Mì", "Recipe 6"),
                result.getContent().stream().map(RecipeSummaryResponse::getTitle).toList());
        assertTrue(result.isHasNext());
        assertEquals("2", result.getNextCursor());
    }

    @Test
    void searchByName_secondPageDoesNotRepeatLocalResults() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of());
        when(recipeProviderClient.search(any(), eq(5), eq(5)))
                .thenReturn(new ExternalRecipePage(List.of(summary("9", 20, null, null)), 6));

        CursorResponse<RecipeSummaryResponse> result = recipeService.searchByName("mì", "5", 5);

        verify(localRecipeSearchService, never()).searchByName(any(), anyString(), anyCollection());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchByName_providerDownButLocalHasResults_returnsLocalOnly() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of());
        when(localRecipeSearchService.searchByName(eq(userId), eq("mì"), anyCollection()))
                .thenReturn(List.of(local("Mì hoành thánh", null)));
        when(recipeProviderClient.search(any(), eq(0), eq(20))).thenThrow(new RecipeProviderException());

        CursorResponse<RecipeSummaryResponse> result = recipeService.searchByName("mì", null, 20);

        assertEquals(1, result.getContent().size());
        assertFalse(result.isHasNext());
        assertNull(result.getNextCursor());
    }

    @Test
    void searchByName_providerDownAndNoLocalResults_rethrows() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of());
        when(recipeProviderClient.search(any(), eq(0), eq(20))).thenThrow(new RecipeProviderException());

        assertThrows(RecipeProviderException.class, () -> recipeService.searchByName("mì", null, 20));
    }

    @Test
    void searchByIngredients_mergesLocalMatchesBeforeProviderResults() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of());
        RecipeSummaryResponse mine = local("Trứng chiên", null);
        when(localRecipeSearchService.searchByIngredients(eq(userId), eq(List.of("trứng")), anyCollection(), any(), any(), any()))
                .thenReturn(List.of(mine));
        when(recipeProviderClient.search(any(), eq(0), eq(20)))
                .thenReturn(new ExternalRecipePage(List.of(summary("1", 20, 1, 0)), 1));

        CursorResponse<RecipeSummaryResponse> result =
                recipeService.searchByIngredients(List.of("Trứng"), null, 20, null, null, null);

        assertEquals("Trứng chiên", result.getContent().get(0).getTitle());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void searchByIngredients_noLimitOnIngredientCount() {
        authenticate();
        when(excludedIngredientRepository.findByUserId(userId)).thenReturn(List.of());
        when(recipeProviderClient.search(any(), eq(0), eq(20))).thenReturn(new ExternalRecipePage(List.of(), 0));
        List<String> many = java.util.stream.IntStream.range(0, 30).mapToObj(i -> "nl" + i).toList();

        recipeService.searchByIngredients(many, null, 20, null, null, null);

        ArgumentCaptor<RecipeSearchCriteria> captor = ArgumentCaptor.forClass(RecipeSearchCriteria.class);
        verify(recipeProviderClient).search(captor.capture(), eq(0), eq(20));
        assertEquals(30, captor.getValue().includeIngredients().size());
    }
}
