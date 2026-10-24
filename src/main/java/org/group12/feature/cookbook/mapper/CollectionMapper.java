package org.group12.feature.cookbook.mapper;

import org.group12.feature.cookbook.dto.response.CollectionResponse;
import org.group12.feature.cookbook.entity.Collection;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CollectionMapper {
    CollectionResponse toCollectionResponse(Collection collection, int recipeCount);
}
