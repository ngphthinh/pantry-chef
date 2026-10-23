package org.group12.common.config;

import org.group12.common.exception.AppException;
import org.group12.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Component
public class ContentGuard {

    private final String systemPrompt;

    private final List<String> blockedTerms;

    public ContentGuard(
            PromptLoader promptLoader,
            @Value("${app.ai.blocked-terms}") List<String> blockedTerms) {
        this.systemPrompt = promptLoader.load(
                "prompts/system.st"
        );
        this.blockedTerms = blockedTerms.stream()
                .map(this::normalize)
                .toList();
    }

    public String systemPrompt() {
        return systemPrompt;
    }

    public void checkText(String... values) {
        for (String value : values) {
            if (value != null && containsBlocked(value)) {
                throw new AppException(ErrorCode.CONTENT_BLOCKED);
            }
        }
    }

    public void checkOutput(List<String> values) {
        for (String value : values) {
            if (value != null && containsBlocked(value)) {
                throw new AppException(ErrorCode.CONTENT_BLOCKED);
            }
        }
    }

    private boolean containsBlocked(String text) {
        String normalized = normalize(text);
        return blockedTerms.stream().anyMatch(normalized::contains);
    }

    private String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }
}