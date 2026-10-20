package org.group12.feature.shopping.mapper;

import org.group12.feature.shopping.dto.request.ShoppingListCreateRequest;
import org.group12.feature.shopping.dto.request.ShoppingListUpdateRequest;
import org.group12.feature.shopping.dto.response.ShoppingListDetailResponse;
import org.group12.feature.shopping.dto.response.ShoppingListResponse;
import org.group12.feature.shopping.entity.ShoppingList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShoppingListMapper {

    ShoppingListResponse toResponse(ShoppingList list);

    // items do service gán (lấy bằng query riêng, có thứ tự, không kích hoạt lazy loading)
    @Mapping(target = "items", ignore = true)
    ShoppingListDetailResponse toDetailResponse(ShoppingList list);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "items", ignore = true)
    ShoppingList toEntity(ShoppingListCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(ShoppingListUpdateRequest request, @MappingTarget ShoppingList list);
}
