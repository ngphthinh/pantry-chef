package org.group12.feature.cookbook.service;

import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.cookbook.dto.request.SavedRecipeCreateRequest;
import org.group12.feature.cookbook.dto.request.SavedRecipeUpdateRequest;
import org.group12.feature.cookbook.dto.response.SavedRecipeDetailResponse;
import org.group12.feature.cookbook.dto.response.SavedRecipeResponse;
import org.group12.feature.cookbook.entity.SavedRecipe;
import org.group12.feature.cookbook.exception.SavedRecipeAlreadyExistsException;
import org.group12.feature.cookbook.exception.SavedRecipeNotFoundException;
import org.group12.feature.cookbook.mapper.SavedRecipeMapper;
import org.group12.feature.cookbook.repository.SavedRecipeRepository;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.group12.feature.recipe.service.RecipeService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
@PreAuthorize("hasRole('USER')")
@Service
@RequiredArgsConstructor
public class SavedRecipeService {
    private final SavedRecipeRepository savedRecipeRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService;
    private final UserRepository userRepository;
    private final SavedRecipeMapper mapper;
    private final AppUtil appUtil;

    @Transactional
    public SavedRecipeResponse save(SavedRecipeCreateRequest req) {
        UUID userId = appUtil.userIdFromAuthentication();
        Recipe recipe = resolveRecipe(req);

        boolean existing = savedRecipeRepository.existsByUserIdAndRecipeId(userId, recipe.getId());
        if (existing) {
            throw new SavedRecipeAlreadyExistsException();
        }

        SavedRecipe saved = SavedRecipe.builder()
                .id(appUtil.generateUUID())
                .user(userRepository.getReferenceById(userId))
                .recipe(recipe)
                .favorite(false)
                .build();

        return mapper.toSavedRecipeResponse(savedRecipeRepository.save(saved));
    }

    @Transactional(readOnly = true)
    public CursorResponse<SavedRecipeResponse> list(UUID cursor, int size,
                                                    Boolean favorite, UUID collectionId) {
        UUID userId = appUtil.userIdFromAuthentication();
        List<SavedRecipe> rows = savedRecipeRepository.findPage(
                userId, cursor, favorite, collectionId, PageRequest.of(0, size + 1));

        return appUtil.buildCursorResponse(rows, size, SavedRecipe::getId, mapper::toSavedRecipeResponse);
    }

    private Recipe resolveRecipe(SavedRecipeCreateRequest req) {
        if (req.getRecipeId() != null) {
            return recipeRepository.findById(req.getRecipeId())
                    .orElseThrow(RecipeNotFoundException::new);
        }
        return recipeRepository.findByExternalId(req.getExternalId())
                .orElseGet(() -> importExternal(req.getExternalId()));
    }

    @Transactional(readOnly = true)
    public SavedRecipeDetailResponse getDetail(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        SavedRecipe saved = savedRecipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(SavedRecipeNotFoundException::new);
        return mapper.toSavedRecipeDetailResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        SavedRecipe saved = savedRecipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(SavedRecipeNotFoundException::new);
        savedRecipeRepository.delete(saved);
    }

    @Transactional
    public SavedRecipeResponse update(UUID id, SavedRecipeUpdateRequest req) {
        UUID userId = appUtil.userIdFromAuthentication();
        SavedRecipe saved = savedRecipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(SavedRecipeNotFoundException::new);

        mapper.updateSavedRecipe(req, saved);

        return mapper.toSavedRecipeResponse(savedRecipeRepository.save(saved));
    }

    /** Lấy công thức từ API ngoài và lưu bản sao cục bộ (cùng transaction), rồi trả entity để gắn SavedRecipe. */
    private Recipe importExternal(String externalId) {
        UUID recipeId = recipeService.getByExternalId(externalId).getId();
        return recipeRepository.findById(recipeId).orElseThrow(RecipeNotFoundException::new);
    }

}