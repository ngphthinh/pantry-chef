package org.group12.feature.ingredient.service;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.utils.AppUtil;
import org.group12.common.utils.TextUtil;
import org.group12.feature.ingredient.dto.request.IngredientCreateRequest;
import org.group12.feature.ingredient.dto.request.IngredientUpdateRequest;
import org.group12.feature.ingredient.dto.response.IngredientResponse;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.ingredient.entity.IngredientCategory;
import org.group12.feature.ingredient.exception.IngredientBarcodeDuplicateException;
import org.group12.feature.ingredient.exception.IngredientBarcodeNotFoundException;
import org.group12.feature.ingredient.exception.IngredientCategoryNotFoundException;
import org.group12.feature.ingredient.exception.IngredientNotFoundException;
import org.group12.feature.ingredient.mapper.IngredientMapper;
import org.group12.feature.ingredient.repository.IngredientCategoryRepository;
import org.group12.feature.ingredient.repository.IngredientRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientService {

    private static final int MAX_PAGE_SIZE = 50;

    private final IngredientRepository ingredientRepository;
    private final IngredientCategoryRepository categoryRepository;
    private final IngredientMapper ingredientMapper;
    private final AppUtil appUtil;

    private static final UUID NIL_UUID = new UUID(0L, 0L);

    @Transactional(readOnly = true)
    public CursorResponse<IngredientResponse> searchIngredient(String keyword, UUID categoryId, UUID cursor, int size) {
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String normalized = keyword == null ? "" : TextUtil.normalizeForSearch(keyword);
        String pattern = "%" + escapeLike(normalized) + "%";

        List<Ingredient> items = ingredientRepository.searchIngredients(
                cursor == null ? NIL_UUID : cursor,
                categoryId == null,
                categoryId == null ? NIL_UUID : categoryId,
                pattern,
                PageRequest.of(0, pageSize + 1));

        return appUtil.buildCursorResponse(items, pageSize, Ingredient::getId, ingredientMapper::toIngredientResponse);
    }

    private static String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    @Transactional(readOnly = true)
    public IngredientResponse getIngredientById(UUID id) {
        return ingredientMapper.toIngredientResponse(findIngredient(id));
    }

    @Transactional(readOnly = true)
    public IngredientResponse getIngredientByBarcode(String barcode) {
        return ingredientRepository.findByBarcode(barcode.trim())
                .map(ingredientMapper::toIngredientResponse)
                .orElseThrow(IngredientBarcodeNotFoundException::new);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public IngredientResponse createIngredient(IngredientCreateRequest request) {
        IngredientCategory category = findCategory(request.getCategoryId());

        String barcode = request.getBarcode();
        if (barcode != null && ingredientRepository.existsByBarcode(barcode)) {
            throw new IngredientBarcodeDuplicateException();
        }

        Ingredient ingredient = Ingredient.builder()
                .id(appUtil.generateUUID())
                .name(request.getName().trim())
                .defaultUnit(request.getDefaultUnit())
                .barcode(barcode)
                .category(category)
                .build();

        return ingredientMapper.toIngredientResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public IngredientResponse updateIngredient(UUID id, IngredientUpdateRequest request) {
        Ingredient ingredient = findIngredient(id);
        IngredientCategory category = findCategory(request.getCategoryId());

        String barcode = request.getBarcode();
        if (barcode != null && ingredientRepository.existsByBarcodeAndIdNot(barcode, id)) {
            throw new IngredientBarcodeDuplicateException();
        }

        ingredient.setName(request.getName().trim());
        ingredient.setDefaultUnit(request.getDefaultUnit());
        ingredient.setBarcode(barcode);
        ingredient.setCategory(category);

        return ingredientMapper.toIngredientResponse(ingredientRepository.save(ingredient));
    }
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteIngredient(UUID id) {
        ingredientRepository.delete(findIngredient(id));
    }

    private Ingredient findIngredient(UUID id) {
        return ingredientRepository.findById(id).orElseThrow(IngredientNotFoundException::new);
    }

    private IngredientCategory findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(IngredientCategoryNotFoundException::new);
    }
}