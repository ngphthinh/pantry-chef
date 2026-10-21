package org.group12.feature.shopping.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.CursorResponse;
import org.group12.feature.shopping.dto.request.ShoppingListCreateRequest;
import org.group12.feature.shopping.dto.request.ShoppingListUpdateRequest;
import org.group12.feature.shopping.dto.response.ShoppingListDetailResponse;
import org.group12.feature.shopping.dto.response.ShoppingListResponse;
import org.group12.feature.shopping.enums.ShoppingResponseCode;
import org.group12.feature.shopping.service.ShoppingListService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    // POST /v1/shopping-lists
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)

    public ApiResponse<ShoppingListResponse> create(@Valid @RequestBody ShoppingListCreateRequest request) {
        return ApiResponse.<ShoppingListResponse>builder()
                .code(ShoppingResponseCode.SHOPPING_LIST_CREATED.getCode())
                .message(ShoppingResponseCode.SHOPPING_LIST_CREATED.getMessage())
                .data(shoppingListService.create(request))
                .build();
    }

    // GET /v1/shopping-lists?cursor=&size=
    @GetMapping
    public ApiResponse<CursorResponse<ShoppingListResponse>> list(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<CursorResponse<ShoppingListResponse>>builder()
                .code(ShoppingResponseCode.SHOPPING_LISTS_FETCHED.getCode())
                .message(ShoppingResponseCode.SHOPPING_LISTS_FETCHED.getMessage())
                .data(shoppingListService.getMyLists(cursor, size))
                .build();
    }

    // GET /v1/shopping-lists/{id}
    @GetMapping("/{id}")
    public ApiResponse<ShoppingListDetailResponse> detail(@PathVariable UUID id) {
        return ApiResponse.<ShoppingListDetailResponse>builder()
                .code(ShoppingResponseCode.SHOPPING_LIST_FETCHED.getCode())
                .message(ShoppingResponseCode.SHOPPING_LIST_FETCHED.getMessage())
                .data(shoppingListService.getDetail(id))
                .build();
    }

    // PUT /v1/shopping-lists/{id}
    @PutMapping("/{id}")
    public ApiResponse<ShoppingListResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody ShoppingListUpdateRequest request) {
        return ApiResponse.<ShoppingListResponse>builder()
                .code(ShoppingResponseCode.SHOPPING_LIST_UPDATED.getCode())
                .message(ShoppingResponseCode.SHOPPING_LIST_UPDATED.getMessage())
                .data(shoppingListService.update(id, request))
                .build();
    }

    // DELETE /v1/shopping-lists/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        shoppingListService.delete(id);

    }
}
