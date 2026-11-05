package org.group12.feature.ingredient.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.group12.feature.auth.entity.User;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Danh mục nguyên liệu gồm hai loại:
 *  - của hệ thống (ownerId = null): ai cũng xem được, chỉ ADMIN thêm / sửa / xóa;
 *  - của người dùng (ownerId = id người tạo): chỉ chủ sở hữu thấy và thêm / sửa / xóa.
 * Bản ghi không được phép xem hoặc sửa coi như không tồn tại (404).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientCategoryService {

    private final IngredientCategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientCategoryMapper categoryMapper;
    private final AppUtil appUtil;
    private final EntityManager entityManager;
    @Transactional(readOnly = true)
    public List<IngredientCategoryResponse> getAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder")
                .and(Sort.by(Sort.Direction.DESC, "id"));

        UUID userId = appUtil.orNil(currentUserIdOrNull());
        return categoryMapper.toResponses(categoryRepository.findVisible(userId, sort));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public IngredientCategoryResponse create(IngredientCategoryRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();

        if (isCodeTaken(request.getCode(), userId)) {
            throw new IngredientCategoryCodeDuplicateException();
        }

        IngredientCategory category = IngredientCategory.builder()
                .id(appUtil.generateUUID())
                .code(request.getCode())
                .name(request.getName().trim())
                .owner(isAdmin() ? null : entityManager.getReference(User.class, userId))
                .build();

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public IngredientCategoryResponse update(UUID id, IngredientCategoryRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();
        IngredientCategory category = findEditableCategory(id, userId);

        if (!category.getCode().equals(request.getCode())
                && isCodeTaken(request.getCode(), userId)) {
            throw new IngredientCategoryCodeDuplicateException();
        }

        category.setCode(request.getCode());
        category.setName(request.getName().trim());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public void delete(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        IngredientCategory category = findEditableCategory(id, userId);

        if (ingredientRepository.existsAnyByCategoryId(id)) {
            throw new IngredientCategoryInUseException();
        }

        categoryRepository.delete(category);
    }

    private boolean isCodeTaken(String code, UUID userId) {
        return categoryRepository.existsByCodeAndOwnerIsNull(code)
                || categoryRepository.existsByCodeAndOwnerId(code, userId);
    }

    private IngredientCategory findEditableCategory(UUID id, UUID userId) {
        IngredientCategory category = categoryRepository.findVisibleById(id, userId)
                .orElseThrow(IngredientCategoryNotFoundException::new);

        boolean editable = category.getOwner() == null
                ? isAdmin()
                : userId.equals(category.getOwner().getId());
        if (!editable) {
            throw new IngredientCategoryNotFoundException();
        }
        return category;
    }

    private static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private static UUID currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}