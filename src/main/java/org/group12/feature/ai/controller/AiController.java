package org.group12.feature.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.ai.dto.request.ImageUploadRequest;
import org.group12.feature.ai.dto.response.RecognizedIngredientResponse;
import org.group12.feature.ai.enums.AiResponseCode;
import org.group12.feature.ai.service.IngredientRecognitionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final IngredientRecognitionService service;

    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<RecognizedIngredientResponse>> recognize(
            @Valid @ModelAttribute ImageUploadRequest request) {
        return ApiResponse.<List<RecognizedIngredientResponse>>builder()
                .code(AiResponseCode.AI_RECOGNIZE_INGREDIENT_SUCCESS.getCode())
                .message(AiResponseCode.AI_RECOGNIZE_INGREDIENT_SUCCESS.getMessage())
                .data(service.recognize(request))
                .build();
    }
}