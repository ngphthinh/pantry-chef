// service/AiRecipeIdeaService.java
package org.group12.feature.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.group12.common.config.ContentGuard;
import org.group12.common.config.PromptLoader;
import org.group12.common.exception.AppException;
import org.group12.common.utils.AppUtil;
import org.group12.feature.ai.dto.request.RecipeIdeasRequest;
import org.group12.feature.ai.dto.response.RecipeIdeaResponse;
import org.group12.feature.ai.exception.AiProviderException;
import org.group12.feature.pantry.exception.PantryEmptyException;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@PreAuthorize("hasRole('USER')")
public class AiRecipeIdeaService {

    private static final int MAX_PANTRY_INGREDIENTS = 50;

    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final ContentGuard contentGuard;
    private final PantryItemRepository pantryItemRepository;
    private final AppUtil appUtil;
    private final String promptPath;
    private final double minConfidence;
    private final int maxResults;

    public AiRecipeIdeaService(
            ChatClient.Builder builder,
            PromptLoader promptLoader,
            ContentGuard contentGuard,
            PantryItemRepository pantryItemRepository,
            AppUtil appUtil,
            @Value("${app.ai.prompt.path.recipe-ideas}") String promptPath,
            @Value("${app.ai.min-confidence}") double minConfidence,
            @Value("${app.ai.recipe-ideas.max-results}") int maxResults) {
        this.chatClient = builder.build();
        this.promptLoader = promptLoader;
        this.contentGuard = contentGuard;
        this.pantryItemRepository = pantryItemRepository;
        this.appUtil = appUtil;
        this.promptPath = promptPath;
        this.minConfidence = minConfidence;
        this.maxResults = maxResults;
    }

    public List<RecipeIdeaResponse> suggest(RecipeIdeasRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();

        List<String> ingredients = pantryItemRepository
                .findDistinctIngredientNames(userId, PageRequest.of(0, MAX_PANTRY_INGREDIENTS));
        if (ingredients.isEmpty()) {
            throw new PantryEmptyException();
        }

        String cuisine = normalize(request.getCuisine());
        contentGuard.checkText(cuisine == null ? "" : cuisine);          // 422
        String template = promptLoader.load(promptPath);

        List<RecipeIdeaResponse> result;
        try {
            result = chatClient.prompt()
                    .system(contentGuard.systemPrompt())
                    .user(u -> u.text(template)
                            .param("ingredients", String.join(", ", ingredients))
                            .param("limit", request.getLimit())
                            .param("cuisine", cuisine == null ? "any" : cuisine)
                            .param("maxMinutes", request.getMaxCookingMinutes() == null
                                    ? "no limit"
                                    : request.getMaxCookingMinutes()))
                    .call()
                    .entity(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Recipe ideas suggestion failed", e);
            throw new AiProviderException();                              // 502
        }

        if (result == null) {
            return List.of();
        }

        int limit = Math.min(request.getLimit(), maxResults);
        List<RecipeIdeaResponse> filtered = result.stream()
                .filter(r -> r.getName() != null && !r.getName().isBlank())
                .filter(r -> r.getConfidence() == null || r.getConfidence() >= minConfidence)
                .sorted(Comparator.comparing(
                        (RecipeIdeaResponse r) -> r.getConfidence() == null ? 0.0 : r.getConfidence())
                        .reversed())
                .limit(limit)
                .toList();

        contentGuard.checkOutput(filtered.stream()
                .flatMap(this::textsOf)
                .toList());                                               // 422

        return filtered;
    }

    private Stream<String> textsOf(RecipeIdeaResponse idea) {
        return Stream.of(
                        Stream.of(idea.getName(), idea.getDescription()),
                        idea.getUsedIngredients() == null ? Stream.<String>empty() : idea.getUsedIngredients().stream(),
                        idea.getMissingIngredients() == null ? Stream.<String>empty() : idea.getMissingIngredients().stream())
                .flatMap(s -> s)
                .filter(Objects::nonNull);
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.strip();
    }
}