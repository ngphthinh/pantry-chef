package org.group12.common.config;

import lombok.extern.slf4j.Slf4j;
import org.group12.common.exception.AppException;
import org.group12.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ContentGuard {

    private static final String B_START = "(?<![\\p{L}\\p{N}])";
    private static final String B_END = "(?![\\p{L}\\p{N}])";

    private final String systemPrompt;
    private final Pattern strictPattern; // diacritics preserved (output)
    private final Pattern loosePattern;  // diacritics stripped (input)

    public ContentGuard(
            PromptLoader promptLoader,
            @Value("${app.ai.blocked-terms}") List<String> blockedTerms) {
        this.systemPrompt = promptLoader.load("prompts/system.st");
        this.strictPattern = buildPattern(
                blockedTerms.stream().map(this::canonical).toList());
        this.loosePattern = buildPattern(
                blockedTerms.stream().map(this::stripAccents).toList());
    }

    public String systemPrompt() {
        return systemPrompt;
    }

    /** Input check: accent-insensitive, so "thuoc" typed without diacritics is still caught. */
    public void checkText(String... values) {
        for (String value : values) {
            if (value == null) continue;
            String hit = find(loosePattern, stripAccents(value));
            if (hit != null) {
                log.error("Blocked term '{}' in input: {}", hit, value);
                throw new AppException(ErrorCode.CONTENT_BLOCKED);
            }
        }
    }

    /** Output check: diacritic-aware, so "thuộc" (belong) != "thuốc" (medicine). */
    public void checkOutput(List<String> values) {
        for (String value : values) {
            if (value == null) continue;
            String hit = find(strictPattern, canonical(value));
            if (hit != null) {
                log.error("Blocked term '{}' in output: {}", hit, value);
                throw new AppException(ErrorCode.CONTENT_BLOCKED);
            }
        }
    }

    private static String find(Pattern p, String text) {
        if (p == null) return null;
        Matcher m = p.matcher(text);
        return m.find() ? m.group() : null;
    }

    private static Pattern buildPattern(List<String> terms) {
        List<String> alts = terms.stream()
                .filter(t -> !t.isBlank())
                .distinct()
                .map(t -> t.endsWith("*")
                        ? Pattern.quote(t.substring(0, t.length() - 1)) + "\\p{L}*"
                        : Pattern.quote(t))
                .toList();
        if (alts.isEmpty()) return null;
        return Pattern.compile(
                B_START + "(?:" + String.join("|", alts) + ")" + B_END,
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    private String canonical(String text) {
        return Normalizer.normalize(text.trim(), Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);
    }

    private String stripAccents(String text) {
        return Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }
}