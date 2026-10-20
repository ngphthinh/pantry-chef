package org.group12.feature.shopping.mapper;

import org.group12.feature.shopping.dto.request.ShoppingItemCreateRequest;
import org.group12.feature.shopping.dto.request.ShoppingItemUpdateRequest;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.group12.feature.shopping.entity.ShoppingItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ShoppingItemMapper {

    ShoppingItemResponse toResponse(ShoppingItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "shoppingList", ignore = true)
    ShoppingItem toEntity(ShoppingItemCreateRequest request);

    // PATCH: bỏ qua trường null để giữ nguyên giá trị cũ
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shoppingList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(ShoppingItemUpdateRequest request, @MappingTarget ShoppingItem item);
}
