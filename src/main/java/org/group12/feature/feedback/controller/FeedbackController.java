package org.group12.feature.feedback.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.feature.feedback.dto.FeedbackCreateRequest;
import org.group12.feature.feedback.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}