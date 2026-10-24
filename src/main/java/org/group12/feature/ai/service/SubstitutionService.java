package org.group12.feature.ai.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.config.ContentGuard;
import org.group12.common.config.PromptLoader;
import org.group12.common.exception.AppException;
import org.group12.common.exception.ErrorCode;
import org.group12.common.utils.AppUtil;
import org.group12.feature.ai.dto.request.SubstitutionRequest;
import org.group12.feature.ai.dto.response.SubstitutionResponse;
import org.group12.feature.ai.exception.AiProviderException;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
public class SubstitutionService {

    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final ContentGuard contentGuard;
    private final RecipeRepository recipeRepository;   // bỏ nếu recipe chỉ đến từ API ngoài
    private final String promptPath;
    private final double minConfidence;
    private final int maxResults;
    private final AppUtil appUtil;

    public SubstitutionService(
            ChatClient.Builder builder,
            PromptLoader promptLoader,
            ContentGuard contentGuard,
            RecipeRepository recipeRepository,
            @Value("${app.ai.prompt.path.substitutions}") String promptPath,
            @Value("${app.ai.min-confidence}") double minConfidence,
            @Value("${app.ai.substitution.max-results}") int maxResults, AppUtil appUtil) {
        this.chatClient = builder.build();
        this.promptLoader = promptLoader;
        this.contentGuard = contentGuard;
        this.recipeRepository = recipeRepository;
        this.promptPath = promptPath;
        this.minConfidence = minConfidence;
        this.maxResults = maxResults;
        this.appUtil = appUtil;
    }

    public List<SubstitutionResponse> suggest(SubstitutionRequest request) {
        String ingredient = request.getMissingIngredient().strip();
        UUID currentUserId = appUtil.userIdFromAuthentication();
        String recipe = resolveRecipeTitle(request, currentUserId);

        contentGuard.checkText(ingredient, recipe);                     // 422
        String template = promptLoader.load(promptPath);

        List<SubstitutionResponse> result;
        try {
            result = chatClient.prompt()
                    .system(contentGuard.systemPrompt())
                    .user(u -> u.text(template)
                            .param("recipe", recipe)
                            .param("ingredient", ingredient))
                    .call()
                    .entity(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Substitution suggestion failed", e);
            throw new AiProviderException();        // 502

        }

        if (result == null) {
            return List.of();
        }

        List<SubstitutionResponse> filtered = result.stream()
                .filter(r -> r.getName() != null && !r.getName().isBlank())
                .filter(r -> r.getConfidence() == null || r.getConfidence() >= minConfidence)
                .sorted(Comparator.comparing(
                        (SubstitutionResponse r) -> r.getConfidence() == null ? 0.0 : r.getConfidence())
                        .reversed())
                .limit(maxResults)                                      // model có thể trả hơn 3
                .toList();

        contentGuard.checkOutput(filtered.stream()
                .flatMap(r -> Stream.of(r.getName(), r.getRatio(), r.getNote()))
                .toList());                                             // 422

        return filtered;
    }

    private String resolveRecipeTitle(SubstitutionRequest request, UUID currentUserId) {
        if (request.getRecipeTitle() != null && !request.getRecipeTitle().isBlank()) {
            return request.getRecipeTitle().strip();
        }

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found: " + request.getRecipeId()));

        // công thức custom chỉ chủ sở hữu được dùng
        if (recipe.isCustom()
                && (recipe.getOwner() == null || !recipe.getOwner().getId().equals(currentUserId))) {
            throw new EntityNotFoundException("Recipe not found: " + request.getRecipeId());
        }
        return recipe.getTitle();
    }
}