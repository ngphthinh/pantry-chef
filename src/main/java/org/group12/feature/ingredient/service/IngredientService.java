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

    /**
     * Tìm / gợi ý nguyên liệu. Phân trang cursor theo id (UUID v7 tăng dần theo thời gian tạo).
     */
    @Transactional(readOnly = true)
    public CursorResponse<IngredientResponse> search(String keyword, UUID categoryId, UUID cursor, int size) {
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        // lấy dư 1 phần tử để biết còn trang sau hay không
        List<Ingredient> items = ingredientRepository
                .findAll(buildSearchSpec(keyword, categoryId, cursor), PageRequest.of(0, pageSize + 1, Sort.by("id")))
                .getContent();

        return appUtil.buildCursorResponse(items, pageSize, Ingredient::getId, ingredientMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public IngredientResponse getById(UUID id) {
        return ingredientMapper.toResponse(findIngredient(id));
    }

    @Transactional(readOnly = true)
    public IngredientResponse getByBarcode(String barcode) {
        return ingredientRepository.findByBarcode(barcode.trim())
                .map(ingredientMapper::toResponse)
                .orElseThrow(IngredientBarcodeNotFoundException::new);
    }

    @Transactional
    public IngredientResponse create(IngredientCreateRequest request) {
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

        return ingredientMapper.toResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientResponse update(UUID id, IngredientUpdateRequest request) {
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

        return ingredientMapper.toResponse(ingredientRepository.save(ingredient));
    }

    /**
     * Xóa mềm (qua @SQLDelete trên entity).
     */
    @Transactional
    public void delete(UUID id) {
        ingredientRepository.delete(findIngredient(id));
    }

    private Ingredient findIngredient(UUID id) {
        return ingredientRepository.findById(id).orElseThrow(IngredientNotFoundException::new);
    }

    private IngredientCategory findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(IngredientCategoryNotFoundException::new);
    }

    private Specification<Ingredient> buildSearchSpec(String keyword, UUID categoryId, UUID cursor) {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("category", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + escapeLike(TextUtil.normalizeForSearch(keyword)) + "%";
                predicates.add(cb.like(root.get("searchName"), pattern, '\\'));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (cursor != null) {
                predicates.add(cb.greaterThan(root.<UUID>get("id"), cursor));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}