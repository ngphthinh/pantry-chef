package org.group12.feature.ingredient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.utils.AppUtil;
import org.group12.feature.ingredient.dto.request.IngredientCategoryRequest;
import org.group12.feature.ingredient.dto.response.IngredientCategoryResponse;
import org.group12.feature.ingredient.entity.IngredientCategory;
import org.group12.feature.ingredient.exception.IngredientCategoryCodeDuplicateException;
import org.group12.feature.ingredient.exception.IngredientCategoryInUseException;
import org.group12.feature.ingredient.exception.IngredientCategoryNotFoundException;
import org.group12.feature.ingredient.mapper.IngredientCategoryMapper;
import org.group12.feature.ingredient.repository.IngredientCategoryRepository;
import org.group12.feature.ingredient.repository.IngredientRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientCategoryService {

    private final IngredientCategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientCategoryMapper categoryMapper;
    private final AppUtil appUtil;


    @Transactional(readOnly = true)
    public List<IngredientCategoryResponse> getIngredientCategories() {
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.DESC, "id"));
        return categoryMapper.toIngredientCategoryResponses(categoryRepository.findAll(sort));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public IngredientCategoryResponse createIngredientCategory(IngredientCategoryRequest request) {
        if (categoryRepository.existsByCode(request.getCode())) {
            throw new IngredientCategoryCodeDuplicateException();
        }

        IngredientCategory category = IngredientCategory.builder()
                .id(appUtil.generateUUID())
                .code(request.getCode())
                .name(request.getName().trim())
                .build();

        return categoryMapper.toIngredientCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public IngredientCategoryResponse updateIngredientCategory(UUID id, IngredientCategoryRequest request) {
        IngredientCategory category = findCategory(id);

        if (!category.getCode().equals(request.getCode()) && categoryRepository.existsByCode(request.getCode())) {
            throw new IngredientCategoryCodeDuplicateException();
        }

        category.setCode(request.getCode());
        category.setName(request.getName().trim());

        return categoryMapper.toIngredientCategoryResponse(categoryRepository.save(category));
    }
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteIngredientCategory(UUID id) {
        IngredientCategory category = findCategory(id);

        if (ingredientRepository.existsAnyByCategoryId(id)) {
            throw new IngredientCategoryInUseException();
        }

        categoryRepository.delete(category);
    }

    private IngredientCategory findCategory(UUID id) {
        return categoryRepository.findById(id).orElseThrow(IngredientCategoryNotFoundException::new);
    }
}