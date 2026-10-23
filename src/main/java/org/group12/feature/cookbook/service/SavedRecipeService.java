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
import org.group12.feature.cookbook.exception.RecipeNotFoundException;
import org.group12.feature.cookbook.exception.SavedRecipeNotFoundException;
import org.group12.feature.cookbook.mapper.SavedRecipeMapper;
import org.group12.feature.cookbook.repository.RecipeRepository;
import org.group12.feature.cookbook.repository.SavedRecipeRepository;
import org.group12.feature.recipe.entity.Recipe;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavedRecipeService {
    private final SavedRecipeRepository savedRecipeRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final SavedRecipeMapper mapper;
    private final AppUtil appUtil;

    @Transactional
    public CreateResult save(SavedRecipeCreateRequest req) {
        UUID userId = appUtil.userIdFromAuthentication();
        Recipe recipe = resolveRecipe(req);

        Optional<SavedRecipe> existing =
                savedRecipeRepository.findByUserIdAndRecipeId(userId, recipe.getId());
        if (existing.isPresent()) {
            return new CreateResult(mapper.toResponse(existing.get()), false);
        }

        SavedRecipe saved = SavedRecipe.builder()
                .id(UUID.randomUUID())
                .user(userRepository.getReferenceById(userId))
                .recipe(recipe)
                .favorite(false)
                .build();

        try {
            saved = savedRecipeRepository.saveAndFlush(saved);
        } catch (DataIntegrityViolationException e) {
            return new CreateResult(
                    mapper.toResponse(savedRecipeRepository
                            .findByUserIdAndRecipeId(userId, recipe.getId())
                            .orElseThrow(() -> e)),
                    false);
        }
        return new CreateResult(mapper.toResponse(saved), true);
    }

    @Transactional(readOnly = true)
    public CursorResponse<SavedRecipeResponse> list(UUID cursor, int size,
                                                    Boolean favorite, UUID collectionId) {
        UUID userId = appUtil.userIdFromAuthentication();
        List<SavedRecipe> rows = savedRecipeRepository.findPage(
                userId, cursor, favorite, collectionId, PageRequest.of(0, size + 1));

        return appUtil.buildCursorResponse(rows, size, SavedRecipe::getId, mapper::toResponse);
    }

    private Recipe resolveRecipe(SavedRecipeCreateRequest req) {
        if (req.recipeId() != null) {
            return recipeRepository.findById(req.recipeId())
                    .orElseThrow(RecipeNotFoundException::new);
        }
        return recipeRepository.findByExternalId(req.externalId())
                .orElseGet(() -> importExternal(req.externalId()));
    }

    @Transactional(readOnly = true)
    public SavedRecipeDetailResponse getDetail(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        SavedRecipe saved = savedRecipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(SavedRecipeNotFoundException::new);
        return mapper.toDetailResponse(saved);
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

        if (req.rating() != null) saved.setRating(req.rating());
        if (req.note() != null) saved.setNote(req.note().isBlank() ? null : req.note().trim());
        if (req.favorite() != null) saved.setFavorite(req.favorite());

        return mapper.toResponse(savedRecipeRepository.save(saved));
    }

    private Recipe importExternal(String externalId) {
        throw new RecipeNotFoundException();
    }

    public record CreateResult(SavedRecipeResponse body, boolean created) {}
}