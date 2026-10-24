package org.group12.feature.pantry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.CursorResponse;
import org.group12.feature.pantry.dto.request.*;
import org.group12.feature.pantry.dto.response.PantryItemResponse;
import org.group12.feature.pantry.enums.PantryResponseCode;
import org.group12.feature.pantry.enums.StorageLocation;
import org.group12.feature.pantry.service.PantryItemService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/pantry-items")
@RequiredArgsConstructor

public class PantryItemController {

    private final PantryItemService pantryItemService;

    // POST /v1/pantry-items
    @PostMapping
    public ApiResponse<PantryItemResponse> create(@Valid @RequestBody PantryItemCreateRequest request) {
        return ApiResponse.<PantryItemResponse>builder()
                .code(PantryResponseCode.PANTRY_ITEM_CREATED.getCode())
                .message(PantryResponseCode.PANTRY_ITEM_CREATED.getMessage())
                .data(pantryItemService.create(request))
                .build();
    }

    // POST /v1/pantry-items/batch
    @PostMapping("/batch")
    public ApiResponse<List<PantryItemResponse>> createBatch(@Valid @RequestBody PantryItemBatchCreateRequest request) {
        return ApiResponse.<List<PantryItemResponse>>builder()
                .code(PantryResponseCode.PANTRY_ITEMS_CREATED.getCode())
                .message(PantryResponseCode.PANTRY_ITEMS_CREATED.getMessage())
                .data(pantryItemService.createBatch(request))
                .build();
    }

    // GET /v1/pantry-items?cursor=&size=&categoryId=&location=&keyword=
    @GetMapping
    public ApiResponse<CursorResponse<PantryItemResponse>> list(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) StorageLocation location,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.<CursorResponse<PantryItemResponse>>builder()
                .code(PantryResponseCode.PANTRY_ITEMS_FETCHED.getCode())
                .message(PantryResponseCode.PANTRY_ITEMS_FETCHED.getMessage())
                .data(pantryItemService.getMyItems(cursor, size, categoryId, location, keyword))
                .build();
    }

    // GET /v1/pantry-items/expiring?days=3
    @GetMapping("/expiring")
    public ApiResponse<List<PantryItemResponse>> getExpiringItems(
            @RequestParam(defaultValue = "3") int days
    ) {
        return ApiResponse.<List<PantryItemResponse>>builder()
                .code(PantryResponseCode.PANTRY_ITEMS_FETCHED.getCode())
                .message(PantryResponseCode.PANTRY_ITEMS_FETCHED.getMessage())
                .data(pantryItemService.getExpiringItems(days))
                .build();
    }

    // GET /v1/pantry-items/{id}
    @GetMapping("/{id}")
    public ApiResponse<PantryItemResponse> getById(@PathVariable UUID id) {
        return ApiResponse.<PantryItemResponse>builder()
                .code(PantryResponseCode.PANTRY_ITEM_FETCHED.getCode())
                .message(PantryResponseCode.PANTRY_ITEM_FETCHED.getMessage())
                .data(pantryItemService.getById(id))
                .build();
    }

    // PUT /v1/pantry-items/{id}
    @PutMapping("/{id}")
    public ApiResponse<PantryItemResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody PantryItemUpdateRequest request
    ) {
        return ApiResponse.<PantryItemResponse>builder()
                .code(PantryResponseCode.PANTRY_ITEM_UPDATED.getCode())
                .message(PantryResponseCode.PANTRY_ITEM_UPDATED.getMessage())
                .data(pantryItemService.update(id, request))
                .build();
    }

    // DELETE /v1/pantry-items/{id}
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        pantryItemService.delete(id);
        return ApiResponse.<Void>builder()
                .code(PantryResponseCode.PANTRY_ITEM_DELETED.getCode())
                .message(PantryResponseCode.PANTRY_ITEM_DELETED.getMessage())
                .build();
    }

    // POST /v1/pantry-items/{id}/photo
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PantryItemResponse> uploadPhoto(
            @PathVariable UUID id,
            @ModelAttribute ImageUploadRequest image
            ) {
        return ApiResponse.<PantryItemResponse>builder()
                .code(PantryResponseCode.PANTRY_ITEM_PHOTO_UPLOADED.getCode())
                .message(PantryResponseCode.PANTRY_ITEM_PHOTO_UPLOADED.getMessage())
                .data(pantryItemService.uploadPhoto(id, image))
                .build();
    }

    // POST /v1/pantry-items/{id}/consume
    @PostMapping("/{id}/consume")
    public ApiResponse<PantryItemResponse> consume(
            @PathVariable UUID id,
            @Valid @RequestBody PantryItemConsumeRequest request
    ) {
        return ApiResponse.<PantryItemResponse>builder()
                .code(PantryResponseCode.PANTRY_ITEM_CONSUMED.getCode())
                .message(PantryResponseCode.PANTRY_ITEM_CONSUMED.getMessage())
                .data(pantryItemService.consume(id, request))
                .build();
    }
}
