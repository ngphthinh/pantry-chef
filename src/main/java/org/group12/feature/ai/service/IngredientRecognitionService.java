package org.group12.feature.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.group12.common.config.ContentGuard;
import org.group12.common.config.PromptLoader;
import org.group12.feature.ai.dto.request.ImageUploadRequest;
import org.group12.feature.ai.dto.response.RecognizedIngredientResponse;
import org.group12.feature.ai.exception.AiProviderException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
public class IngredientRecognitionService {

    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final ContentGuard contentGuard;
    private final String promptPath;
    private final double minConfidence;

    public IngredientRecognitionService(
            ChatClient.Builder builder,
            PromptLoader promptLoader,
            ContentGuard contentGuard,
            @Value("${app.ai.prompt.path.recognize-ingredients}") String promptPath,
            @Value("${app.ai.min-confidence}") double minConfidence) {
        this.chatClient = builder.build();
        this.promptLoader = promptLoader;
        this.contentGuard = contentGuard;
        this.promptPath = promptPath;
        this.minConfidence = minConfidence;
    }

    @PreAuthorize("hasRole('USER')")
    public List<RecognizedIngredientResponse> recognize(ImageUploadRequest request) {
        MultipartFile image = request.getImage();
        String prompt = promptLoader.load(promptPath);

        List<RecognizedIngredientResponse> result;
        try {
            result = chatClient.prompt()
                    .system(contentGuard.systemPrompt())
                    .user(u -> u.text(prompt)
                            .media(MimeTypeUtils.parseMimeType(Objects.requireNonNull(image.getContentType())), image.getResource()))
                    .call()
                    .entity(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Ingredient recognition failed", e);
            throw new AiProviderException();        // 502
        }

        if (result == null) {
            return List.of();
        }

        List<RecognizedIngredientResponse> filtered = result.stream()
                .filter(r -> r.getName() != null && !r.getName().isBlank())
                .filter(r -> r.getConfidence() == null || r.getConfidence() >= minConfidence)
                .toList();

        contentGuard.checkOutput(filtered.stream()
                .flatMap(r -> Stream.of(r.getName(), r.getNameEn(), r.getQuantityHint()))
                .toList());                                     // 422

        return filtered;
    }
}