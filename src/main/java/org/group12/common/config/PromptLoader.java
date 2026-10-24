package org.group12.common.config;

import lombok.extern.slf4j.Slf4j;
import org.group12.common.exception.AppException;
import org.group12.common.exception.ErrorCode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class PromptLoader {

    private final ResourceLoader resourceLoader;

    public PromptLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String load(String path) {
        log.info("Loading prompt from path: {}", path);
        Resource resource = resourceLoader.getResource(
                "classpath:" + path
        );

        try (InputStream inputStream = resource.getInputStream()) {
            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new AppException(ErrorCode.LOAD_PROMPT_FAILED);
        }
    }
}