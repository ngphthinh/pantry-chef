package org.group12.feature.recipe.mapper;

import org.group12.feature.recipe.dto.request.CuisineRequest;
import org.group12.feature.recipe.dto.response.CuisineResponse;
import org.group12.feature.recipe.entity.Cuisine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CuisineMapper {

    CuisineResponse toCuisineResponse(Cuisine cuisine);

    @Mapping(target = "id", ignore = true)
    Cuisine toCuisine(CuisineRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCuisineEntity(CuisineRequest request, @MappingTarget Cuisine cuisine);
}
