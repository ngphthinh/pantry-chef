package org.group12.feature.recipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.utils.AppUtil;
import org.group12.feature.recipe.dto.request.CuisineRequest;
import org.group12.feature.recipe.dto.response.CuisineResponse;
import org.group12.feature.recipe.exception.CuisineCodeDuplicateException;
import org.group12.feature.recipe.exception.CuisineInUseException;
import org.group12.feature.recipe.exception.CuisineNotFoundException;
import org.group12.feature.recipe.mapper.CuisineMapper;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.group12.feature.recipe.entity.Cuisine;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuisineService {

    private final CuisineRepository cuisineRepository;
    private final CuisineMapper cuisineMapper;
    private final AppUtil appUtil;

    @Transactional(readOnly = true)
    public List<CuisineResponse> getAll() {
        return cuisineRepository.findAllByOrderByNameAsc().stream()
                .map(cuisineMapper::toCuisineResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CuisineResponse create(CuisineRequest request) {
        if (cuisineRepository.existsByCode(request.getCode())) {
            throw new CuisineCodeDuplicateException();
        }

        Cuisine cuisine = cuisineMapper.toCuisine(request);
        cuisine.setId(appUtil.generateUUID());
        cuisine.setName(cuisine.getName().trim());

        cuisine = cuisineRepository.save(cuisine);
        log.info("Created cuisine {} ({})", cuisine.getCode(), cuisine.getId());
        return cuisineMapper.toCuisineResponse(cuisine);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CuisineResponse update(UUID id, CuisineRequest request) {
        Cuisine cuisine = findCuisine(id);
        if (cuisineRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new CuisineCodeDuplicateException();
        }

        cuisineMapper.updateCuisineEntity(request, cuisine);
        cuisine.setName(cuisine.getName().trim());
        return cuisineMapper.toCuisineResponse(cuisine);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(UUID id) {
        Cuisine cuisine = findCuisine(id);
        if (cuisineRepository.isUsedByRecipe(id)) {
            throw new CuisineInUseException();
        }
        cuisineRepository.delete(cuisine);
        log.info("Deleted cuisine {}", id);
    }

    private Cuisine findCuisine(UUID id) {
        return cuisineRepository.findById(id).orElseThrow(CuisineNotFoundException::new);
    }
}
