package org.group12.feature.feedback.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.CursorResponse;
import org.group12.feature.feedback.dto.request.FeedbackCreateRequest;
import org.group12.feature.feedback.dto.response.FeedbackResponse;
import org.group12.feature.feedback.enums.FeedbackResponseCode;
import org.group12.feature.feedback.enums.FeedbackType;
import org.group12.feature.feedback.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService service;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void create(@Valid @RequestBody FeedbackCreateRequest request) {


        service.create(request);
    }

    @GetMapping
    public ApiResponse<CursorResponse<FeedbackResponse>> getAll(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(30) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) FeedbackType type) {
        return ApiResponse.<CursorResponse<FeedbackResponse>>builder()
                .code(FeedbackResponseCode.GET_FEEDBACKS_SUCCESS.getCode())
                .message(FeedbackResponseCode.GET_FEEDBACKS_SUCCESS.getMessage())
                .data(service.getAllFeedback(cursor, size, keyword, type))
                .build();
    }
}