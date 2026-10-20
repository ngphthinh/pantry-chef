package org.group12.feature.cuisine.mapper;

import org.group12.feature.cuisine.dto.request.CuisineRequest;
import org.group12.feature.cuisine.dto.response.CuisineResponse;
import org.group12.feature.recipe.entity.Cuisine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CuisineMapper {

    CuisineResponse toResponse(Cuisine cuisine);

    @Mapping(target = "id", ignore = true)
    Cuisine toEntity(CuisineRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CuisineRequest request, @MappingTarget Cuisine cuisine);
}
