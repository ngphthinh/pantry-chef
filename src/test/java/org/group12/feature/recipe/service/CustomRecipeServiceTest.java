package org.group12.feature.recipe.service;

import org.group12.common.dto.response.UploadMediaResponse;
import org.group12.common.enums.MediaType;
import org.group12.common.enums.Unit;
import org.group12.common.service.MediaStorageService;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.entity.User;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.recipe.dto.request.ImageUploadRequest;
import org.group12.feature.recipe.dto.request.RecipeCreateRequest;
import org.group12.feature.recipe.dto.request.RecipeIngredientRequest;
import org.group12.feature.recipe.dto.request.RecipeStepRequest;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.exception.CuisineNotFoundException;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.mapper.RecipeMapper;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomRecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private CuisineRepository cuisineRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RecipeMapper recipeMapper;
    @Mock
    private MediaStorageService mediaStorageService;
    @Mock
    private AppUtil appUtil;

    @InjectMocks
    private CustomRecipeService customRecipeService;

    private UUID userId;
    private UUID recipeId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
    }

    @Test
    void create_setsOwnerCustomFlagAndOrdersSteps() {
        RecipeCreateRequest request = RecipeCreateRequest.builder()
                .title("Thịt kho").servings(4)
                .ingredients(List.of(RecipeIngredientRequest.builder().name("  Thịt ba chỉ ").amount(500.0).unit(Unit.G).build()))
                .steps(List.of(RecipeStepRequest.builder().text("Ướp thịt").build(),
                        RecipeStepRequest.builder().text(" Kho ").timerSeconds(3600).build()))
                .build();
        User owner = User.builder().id(userId).build();
        RecipeDetailResponse response = RecipeDetailResponse.builder().title("Thịt kho").build();

        when(appUtil.generateUUID()).thenAnswer(invocation -> UUID.randomUUID());
        when(recipeMapper.toEntity(request)).thenReturn(Recipe.builder().title("Thịt kho").servings(4).build());
        when(userRepository.getReferenceById(userId)).thenReturn(owner);
        when(recipeRepository.saveAndFlush(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeMapper.toDetailResponse(any(Recipe.class))).thenReturn(response);

        assertSame(response, customRecipeService.create(request));

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).saveAndFlush(captor.capture());
        Recipe saved = captor.getValue();
        assertTrue(saved.isCustom());
        assertEquals(owner, saved.getOwner());
        assertEquals("Thịt ba chỉ", saved.getIngredients().get(0).getName());
        assertEquals(2, saved.getSteps().size());
        assertEquals(1, saved.getSteps().get(0).getStepOrder());
        assertEquals(2, saved.getSteps().get(1).getStepOrder());
        assertEquals("Kho", saved.getSteps().get(1).getText());
        assertEquals(3600, saved.getSteps().get(1).getTimerSeconds());
    }

    @Test
    void create_unknownCuisine_throwsNotFound() {
        UUID cuisineId = UUID.randomUUID();
        RecipeCreateRequest request = RecipeCreateRequest.builder()
                .title("X").servings(1).cuisineId(cuisineId)
                .ingredients(List.of()).steps(List.of()).build();
        when(recipeMapper.toEntity(request)).thenReturn(Recipe.builder().title("X").build());
        when(userRepository.getReferenceById(userId)).thenReturn(User.builder().id(userId).build());
        when(appUtil.generateUUID()).thenReturn(UUID.randomUUID());
        when(cuisineRepository.findById(cuisineId)).thenReturn(Optional.empty());

        assertThrows(CuisineNotFoundException.class, () -> customRecipeService.create(request));
        verify(recipeRepository, never()).saveAndFlush(any());
    }

    @Test
    void getById_otherUsersRecipe_throwsNotFound() {
        when(recipeRepository.findCustomDetailByIdAndOwnerId(recipeId, userId)).thenReturn(Optional.empty());

        assertThrows(RecipeNotFoundException.class, () -> customRecipeService.getById(recipeId));
    }

    @Test
    void delete_ownedRecipe_softDeletes() {
        Recipe recipe = Recipe.builder().id(recipeId).custom(true).build();
        when(recipeRepository.findByIdAndOwnerIdAndCustomTrue(recipeId, userId)).thenReturn(Optional.of(recipe));

        customRecipeService.delete(recipeId);

        verify(recipeRepository).delete(recipe);
    }

    @Test
    void delete_otherUsersRecipe_throwsNotFound() {
        when(recipeRepository.findByIdAndOwnerIdAndCustomTrue(recipeId, userId)).thenReturn(Optional.empty());

        assertThrows(RecipeNotFoundException.class, () -> customRecipeService.delete(recipeId));
        verify(recipeRepository, never()).delete(any());
    }

    @Test
    void getMyRecipes_firstPageFetchesOneExtraRow() {
        customRecipeService.getMyRecipes(null, 20);

        verify(recipeRepository).findByCustomTrueAndOwnerIdOrderByIdDesc(userId, PageRequest.of(0, 21));
    }

    @Test
    void getMyRecipes_nextPageUsesCursorAndClampsSize() {
        UUID cursor = UUID.randomUUID();

        customRecipeService.getMyRecipes(cursor, 1000);

        verify(recipeRepository).findByCustomTrueAndOwnerIdAndIdLessThanOrderByIdDesc(userId, cursor, PageRequest.of(0, 51));
    }

    @Test
    void uploadImage_deletesPreviousImageAndStoresNewOne() {
        Recipe recipe = Recipe.builder().id(recipeId).custom(true).publicImgId("old-id").build();
        MockMultipartFile file = new MockMultipartFile("image", "pho.png", "image/png", new byte[]{1, 2, 3});
        ImageUploadRequest request = ImageUploadRequest.builder().image(file).build();

        when(recipeRepository.findCustomDetailByIdAndOwnerId(recipeId, userId)).thenReturn(Optional.of(recipe));
        when(mediaStorageService.uploadSync(eq(file), any(), eq(MediaType.IMAGE)))
                .thenReturn(UploadMediaResponse.builder().publicId("new-id").url("https://cdn/new.png").build());
        when(recipeRepository.saveAndFlush(recipe)).thenReturn(recipe);
        when(recipeMapper.toDetailResponse(recipe)).thenReturn(RecipeDetailResponse.builder().build());

        customRecipeService.uploadImage(recipeId, request);

        verify(mediaStorageService).delete("old-id");
        assertEquals("https://cdn/new.png", recipe.getImageUrl());
        assertEquals("new-id", recipe.getPublicImgId());
    }
}
