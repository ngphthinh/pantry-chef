package org.group12.feature.cuisine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.utils.AppUtil;
import org.group12.feature.cuisine.dto.request.CuisineRequest;
import org.group12.feature.cuisine.dto.response.CuisineResponse;
import org.group12.feature.cuisine.exception.CuisineCodeDuplicateException;
import org.group12.feature.cuisine.exception.CuisineInUseException;
import org.group12.feature.cuisine.exception.CuisineNotFoundException;
import org.group12.feature.cuisine.mapper.CuisineMapper;
import org.group12.feature.cuisine.repository.CuisineRepository;
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
                .map(cuisineMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CuisineResponse create(CuisineRequest request) {
        if (cuisineRepository.existsByCode(request.getCode())) {
            throw new CuisineCodeDuplicateException();
        }

        Cuisine cuisine = cuisineMapper.toEntity(request);
        cuisine.setId(appUtil.generateUUID());
        cuisine.setName(cuisine.getName().trim());

        cuisine = cuisineRepository.save(cuisine);
        log.info("Created cuisine {} ({})", cuisine.getCode(), cuisine.getId());
        return cuisineMapper.toResponse(cuisine);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CuisineResponse update(UUID id, CuisineRequest request) {
        Cuisine cuisine = findCuisine(id);
        if (cuisineRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new CuisineCodeDuplicateException();
        }

        cuisineMapper.updateEntity(request, cuisine);
        cuisine.setName(cuisine.getName().trim());
        return cuisineMapper.toResponse(cuisine);
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
