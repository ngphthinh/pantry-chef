package org.group12.feature.shopping.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.shopping.dto.request.ShoppingItemCreateRequest;
import org.group12.feature.shopping.dto.request.ShoppingItemUpdateRequest;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.group12.feature.shopping.enums.ShoppingResponseCode;
import org.group12.feature.shopping.service.ShoppingItemService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/shopping-lists/{listId}/items")
@RequiredArgsConstructor
public class ShoppingItemController {

    private final ShoppingItemService shoppingItemService;

    // POST /v1/shopping-lists/{listId}/items
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ApiResponse<ShoppingItemResponse> create(@PathVariable UUID listId,
                                                    @Valid @RequestBody ShoppingItemCreateRequest request) {
        return ApiResponse.<ShoppingItemResponse>builder()
                .code(ShoppingResponseCode.SHOPPING_ITEM_CREATED.getCode())
                .message(ShoppingResponseCode.SHOPPING_ITEM_CREATED.getMessage())
                .data(shoppingItemService.create(listId, request))
                .build();
    }

    // PATCH /v1/shopping-lists/{listId}/items/{itemId}
    @PatchMapping("/{itemId}")
    public ApiResponse<ShoppingItemResponse> update(@PathVariable UUID listId,
                                                    @PathVariable UUID itemId,
                                                    @Valid @RequestBody ShoppingItemUpdateRequest request) {
        return ApiResponse.<ShoppingItemResponse>builder()
                .code(ShoppingResponseCode.SHOPPING_ITEM_UPDATED.getCode())
                .message(ShoppingResponseCode.SHOPPING_ITEM_UPDATED.getMessage())
                .data(shoppingItemService.update(listId, itemId, request))
                .build();
    }

    // DELETE /v1/shopping-lists/{listId}/items/{itemId}
    @DeleteMapping("/{itemId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID listId, @PathVariable UUID itemId) {
        shoppingItemService.delete(listId, itemId);

    }
}
