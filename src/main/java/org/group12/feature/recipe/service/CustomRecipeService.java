package org.group12.feature.recipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.dto.response.UploadMediaResponse;
import org.group12.common.enums.MediaType;
import org.group12.common.service.MediaStorageService;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.recipe.dto.request.ImageUploadRequest;
import org.group12.feature.recipe.dto.request.RecipeCreateRequest;
import org.group12.feature.recipe.dto.request.RecipeIngredientRequest;
import org.group12.feature.recipe.dto.request.RecipeStepRequest;
import org.group12.feature.recipe.dto.request.RecipeUpdateRequest;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.dto.response.RecipeSummaryResponse;
import org.group12.feature.recipe.entity.Cuisine;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.entity.RecipeIngredient;
import org.group12.feature.recipe.entity.RecipeStep;
import org.group12.feature.recipe.exception.CuisineNotFoundException;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.mapper.RecipeMapper;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Công thức do người dùng tự tạo ({@code custom = true}). Mọi thao tác chỉ trên công thức của chính người dùng. */
@Slf4j
@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CustomRecipeService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final String IMAGE_FOLDER = "pantry-chef/recipes";

    private final RecipeRepository recipeRepository;
    private final CuisineRepository cuisineRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;
    private final MediaStorageService mediaStorageService;
    private final AppUtil appUtil;

    @Transactional
    public RecipeDetailResponse create(RecipeCreateRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();

        Recipe recipe = recipeMapper.toEntity(request);
        recipe.setId(appUtil.generateUUID());
        recipe.setCustom(true);
        recipe.setOwner(userRepository.getReferenceById(userId));
        recipe.setCuisine(findCuisine(request.getCuisineId()));
        addIngredients(recipe, request.getIngredients());
        addSteps(recipe, request.getSteps());

        recipe = recipeRepository.saveAndFlush(recipe);
        log.info("User {} created custom recipe {}", userId, recipe.getId());
        return recipeMapper.toDetailResponse(recipe);
    }

    @Transactional(readOnly = true)
    public CursorResponse<RecipeSummaryResponse> getMyRecipes(UUID cursor, int size) {
        UUID userId = appUtil.userIdFromAuthentication();
        int limit = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(0, limit + 1);

        List<Recipe> recipes = cursor == null
                ? recipeRepository.findByCustomTrueAndOwnerIdOrderByIdDesc(userId, pageable)
                : recipeRepository.findByCustomTrueAndOwnerIdAndIdLessThanOrderByIdDesc(userId, cursor, pageable);

        return appUtil.buildCursorResponse(recipes, limit, Recipe::getId, recipeMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public RecipeDetailResponse getById(UUID id) {
        return recipeMapper.toDetailResponse(findOwnedWithDetails(id));
    }

    @Transactional
    public RecipeDetailResponse update(UUID id, RecipeUpdateRequest request) {
        Recipe recipe = findOwnedWithDetails(id);

        recipeMapper.updateEntity(request, recipe);
        recipe.setCuisine(findCuisine(request.getCuisineId()));
        // PUT thay toàn bộ: orphanRemoval xóa dòng cũ
        recipe.getIngredients().clear();
        recipe.getSteps().clear();
        addIngredients(recipe, request.getIngredients());
        addSteps(recipe, request.getSteps());

        recipe = recipeRepository.saveAndFlush(recipe);
        return recipeMapper.toDetailResponse(recipe);
    }

    @Transactional
    public void delete(UUID id) {
        Recipe recipe = findOwned(id);
        recipeRepository.delete(recipe); // @SQLDelete -> xóa mềm
        log.info("Deleted custom recipe {}", id);
    }

    @Transactional
    public RecipeDetailResponse uploadImage(UUID id, ImageUploadRequest request) {
        Recipe recipe = findOwnedWithDetails(id);

        if (recipe.getPublicImgId() != null) {
            mediaStorageService.delete(recipe.getPublicImgId()); // xóa ảnh cũ
        }
        UploadMediaResponse upload = mediaStorageService.uploadSync(request.getImage(), IMAGE_FOLDER, MediaType.IMAGE);
        recipe.setImageUrl(upload.getUrl());
        recipe.setPublicImgId(upload.getPublicId());

        recipe = recipeRepository.saveAndFlush(recipe);
        log.info("Uploaded image for custom recipe {}", id);
        return recipeMapper.toDetailResponse(recipe);
    }

    // ---------- private helpers ----------

    /** Công thức của người khác hoặc không phải công thức tự tạo đều trả NotFound để không lộ sự tồn tại. */
    private Recipe findOwned(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        return recipeRepository.findByIdAndOwnerIdAndCustomTrue(id, userId)
                .orElseThrow(RecipeNotFoundException::new);
    }

    private Recipe findOwnedWithDetails(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        return recipeRepository.findCustomDetailByIdAndOwnerId(id, userId)
                .orElseThrow(RecipeNotFoundException::new);
    }

    private Cuisine findCuisine(UUID cuisineId) {
        if (cuisineId == null) {
            return null;
        }
        return cuisineRepository.findById(cuisineId).orElseThrow(CuisineNotFoundException::new);
    }

    private void addIngredients(Recipe recipe, List<RecipeIngredientRequest> requests) {
        for (RecipeIngredientRequest request : requests) {
            recipe.getIngredients().add(RecipeIngredient.builder()
                    .id(appUtil.generateUUID())
                    .recipe(recipe)
                    .name(request.getName().trim())
                    .amount(request.getAmount())
                    .unit(request.getUnit())
                    .build());
        }
    }

    private void addSteps(Recipe recipe, List<RecipeStepRequest> requests) {
        for (int i = 0; i < requests.size(); i++) {
            RecipeStepRequest request = requests.get(i);
            recipe.getSteps().add(RecipeStep.builder()
                    .id(appUtil.generateUUID())
                    .recipe(recipe)
                    .stepOrder(i + 1)
                    .text(request.getText().trim())
                    .timerSeconds(request.getTimerSeconds())
                    .build());
        }
    }
}
