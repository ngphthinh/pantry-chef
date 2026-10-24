package org.group12.feature.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.group12.common.config.ContentGuard;
import org.group12.common.config.PromptLoader;
import org.group12.common.utils.AppUtil;
import org.group12.feature.ai.dto.request.AdaptRecipeRequest;
import org.group12.feature.ai.dto.response.AiAdaptedRecipe;
import org.group12.feature.ai.dto.response.RecipeDetailResponse;
import org.group12.feature.ai.exception.AiProviderException;
import org.group12.feature.ai.mapper.AdaptRecipeMapper;
import org.group12.feature.ingredient.entity.ExcludedIngredient;
import org.group12.feature.ingredient.repository.ExcludedIngredientRepository;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class AdaptRecipeService {

    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final ContentGuard contentGuard;
    private final RecipeRepository recipeRepository;
    private final ExcludedIngredientRepository excludedIngredientRepository;
    private final AdaptRecipeMapper adaptRecipeMapper;
    private final TransactionTemplate tx;
    private final AppUtil appUtil;
    private final String promptPath;

    public AdaptRecipeService(
            ChatClient.Builder builder,
            PromptLoader promptLoader,
            ContentGuard contentGuard,
            RecipeRepository recipeRepository,
            ExcludedIngredientRepository excludedIngredientRepository,
            AdaptRecipeMapper adaptRecipeMapper,
            PlatformTransactionManager txManager,
            AppUtil appUtil,
            @Value("${app.ai.prompt.path.adapt-recipe}") String promptPath) {
        this.chatClient = builder.build();
        this.promptLoader = promptLoader;
        this.contentGuard = contentGuard;
        this.recipeRepository = recipeRepository;
        this.excludedIngredientRepository = excludedIngredientRepository;
        this.adaptRecipeMapper = adaptRecipeMapper;
        this.tx = new TransactionTemplate(txManager);
        this.appUtil = appUtil;
        this.promptPath = promptPath;
    }

    public RecipeDetailResponse adapt(AdaptRecipeRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();

        RecipeDetailResponse original = loadDetail(request.getRecipeId(), userId);

        List<String> excluded = excludedIngredientRepository.findByUserId(userId).stream()
                .map(ExcludedIngredient::getName)
                .toList();
        if (excluded.isEmpty()) {
            return original;                                         // không có loại trừ → trả nguyên bản
        }
        Set<String> excludedLower = excluded.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        boolean conflicts = original.getIngredients().stream()
                .anyMatch(i -> containsExcluded(i.getName(), excludedLower));
        if (!conflicts) {
            return original;                                         // khỏi tốn một lần gọi AI
        }

        String ingredientsText = original.getIngredients().stream()
                .map(i -> i.getName() + " | " + i.getAmount() + " | " + i.getUnit())
                .collect(Collectors.joining("\n"));
        String stepsText = original.getSteps().stream()
                .map(s -> s.getStepOrder() + ". " + s.getText())
                .collect(Collectors.joining("\n"));

        String template = promptLoader.load(promptPath);
        AiAdaptedRecipe adapted;
        try {
            adapted = chatClient.prompt()
                    .system(contentGuard.systemPrompt())
                    .user(u -> u.text(template)
                            .param("title", original.getTitle())
                            .param("excluded", String.join(", ", excluded))
                            .param("ingredients", ingredientsText)
                            .param("steps", stepsText))
                    .call()
                    .entity(AiAdaptedRecipe.class);
        } catch (Exception e) {
            log.error("Adapt recipe failed", e);
            throw new AiProviderException();                         // 502
        }

        if (adapted == null
                || adapted.getIngredients() == null || adapted.getIngredients().isEmpty()
                || adapted.getSteps() == null || adapted.getSteps().isEmpty()) {
            throw new AiProviderException();
        }

        // model không đảm bảo tuân thủ: kiểm lại nguyên liệu và các bước
        boolean violated = adapted.getIngredients().stream()
                .anyMatch(i -> containsExcluded(i.getName(), excludedLower))
                || adapted.getSteps().stream()
                .anyMatch(s -> containsExcluded(s.getText(), excludedLower));
        if (violated) {
            log.warn("Adapted recipe {} still contains excluded ingredients", request.getRecipeId());
            throw new AiProviderException();
        }

        contentGuard.checkOutput(Stream.concat(
                adapted.getIngredients().stream().map(AiAdaptedRecipe.Ingredient::getName),
                adapted.getSteps().stream().map(AiAdaptedRecipe.Step::getText)).toList());   // 422

        adaptRecipeMapper.applyAdapted(adapted, original);
        return original;
    }

    // transaction ngắn: đọc recipe và map ngay khi session còn mở (collection lazy)
    private RecipeDetailResponse loadDetail(UUID recipeId, UUID userId) {
        return tx.execute(status -> {
            Recipe recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(RecipeNotFoundException::new);
            if (recipe.isCustom()
                    && (recipe.getOwner() == null || !recipe.getOwner().getId().equals(userId))) {
                throw new RecipeNotFoundException();                 // không lộ công thức của người khác
            }
            return adaptRecipeMapper.toDetail(recipe);
        });
    }

    private boolean containsExcluded(String text, Set<String> excludedLower) {
        if (text == null) return false;
        String t = text.toLowerCase(Locale.ROOT);
        return excludedLower.stream().anyMatch(t::contains);
    }
}