package org.group12.feature.ingredient.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.group12.feature.auth.entity.User;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public CursorResponse<IngredientResponse> searchIngredient(
            String keyword, UUID categoryId, UUID cursor, int size) {

        UUID userId = appUtil.userIdFromAuthentication();
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String normalized = keyword == null ? "" : TextUtil.normalizeForSearch(keyword);
        String pattern = "%" + escapeLike(normalized) + "%";

        List<Ingredient> items = ingredientRepository.search(
                pattern,
                categoryId == null,
                appUtil.orNil(categoryId),
                userId,
                appUtil.orNil(cursor),
                PageRequest.of(0, pageSize + 1));

        return appUtil.buildCursorResponse(
                items, pageSize, Ingredient::getId,
                ingredientMapper::toIngredientResponse);
    }

    @Transactional(readOnly = true)
    public IngredientResponse getIngredientById(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        return ingredientMapper.toIngredientResponse(findVisibleIngredient(id, userId));
    }

    @Transactional(readOnly = true)
    public IngredientResponse getIngredientByBarcode(String barcode) {
        UUID userId = appUtil.userIdFromAuthentication();
        return ingredientRepository.findVisibleByBarcode(barcode.trim(), userId)
                .map(ingredientMapper::toIngredientResponse)
                .orElseThrow(IngredientBarcodeNotFoundException::new);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public IngredientResponse createIngredient(IngredientCreateRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();
        IngredientCategory category = findCategory(request.getCategoryId(), appUtil.userIdFromAuthentication());

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
                .owner(isAdmin() ? null : entityManager.getReference(User.class, userId))
                .build();

        return ingredientMapper.toIngredientResponse(
                ingredientRepository.save(ingredient));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public IngredientResponse updateIngredient(UUID id, IngredientUpdateRequest request) {
        Ingredient ingredient = findEditableIngredient(id);
        IngredientCategory category = findCategory(request.getCategoryId(), appUtil.userIdFromAuthentication());

        String barcode = request.getBarcode();
        if (barcode != null && ingredientRepository.existsByBarcodeAndIdNot(barcode, id)) {
            throw new IngredientBarcodeDuplicateException();
        }

        ingredient.setName(request.getName().trim());
        ingredient.setDefaultUnit(request.getDefaultUnit());
        ingredient.setBarcode(barcode);
        ingredient.setCategory(category);

        return ingredientMapper.toIngredientResponse(
                ingredientRepository.save(ingredient));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public void deleteIngredient(UUID id) {
        ingredientRepository.delete(findEditableIngredient(id));
    }

    private Ingredient findVisibleIngredient(UUID id, UUID userId) {
        return ingredientRepository.findVisibleById(id, userId)
                .orElseThrow(IngredientNotFoundException::new);
    }
    private Ingredient findEditableIngredient(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        Ingredient ingredient = findVisibleIngredient(id, userId);

        boolean editable = ingredient.getOwner() == null
                ? isAdmin()
                : userId.equals(ingredient.getOwner().getId());
        if (!editable) {
            throw new IngredientNotFoundException();
        }
        return ingredient;
    }

    private IngredientCategory findCategory(UUID categoryId, UUID userId) {
        return categoryRepository.findVisibleById(categoryId, userId)
                .orElseThrow(IngredientCategoryNotFoundException::new);
    }

    private static boolean isAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    // Dùng '!' làm ký tự escape (khớp với ESCAPE '!' trong IngredientRepository.search)
    private static String escapeLike(String value) {
        return value.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}