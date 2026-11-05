package org.group12.feature.cookbook.mapper;

import org.group12.feature.cookbook.dto.response.CollectionResponse;
import org.group12.feature.cookbook.dto.response.CollectionResponseDetail;
import org.group12.feature.cookbook.entity.Collection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = SavedRecipeMapper.class)
public interface CollectionMapper {
    CollectionResponse toCollectionResponse(Collection collection, int recipeCount);
    @Mapping(target = "id", source = "collection.id")
    @Mapping(target = "name", source = "collection.name")
    @Mapping(target = "recipeCount", source = "count")
    @Mapping(target = "recipes", source = "collection.savedRecipes")
    CollectionResponseDetail toCollectionResponseDetail(Collection collection, int count);
}
