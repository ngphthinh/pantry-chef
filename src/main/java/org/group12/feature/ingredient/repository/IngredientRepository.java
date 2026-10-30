package org.group12.feature.ingredient.repository;

import org.group12.feature.ingredient.entity.Ingredient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    Optional<Ingredient> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, UUID id);

    /** Đã có nguyên liệu của hệ thống chưa (dùng để quyết định có khởi tạo dữ liệu mẫu không). */
    boolean existsByOwnerIsNull();

    /** Nguyên liệu người dùng được thấy: của hệ thống (owner NULL) hoặc do chính họ thêm. */
    @Query("""
            SELECT i FROM Ingredient i
            LEFT JOIN FETCH i.category
            WHERE i.id = :id
              AND (i.owner IS NULL OR i.owner.id = :userId)
            """)
    Optional<Ingredient> findVisibleById(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("""
            SELECT i FROM Ingredient i
            LEFT JOIN FETCH i.category
            WHERE i.barcode = :barcode
              AND (i.owner IS NULL OR i.owner.id = :userId)
            """)
    Optional<Ingredient> findVisibleByBarcode(@Param("barcode") String barcode, @Param("userId") UUID userId);

    /**
     * Native query để tính cả nguyên liệu đã xóa mềm: chúng vẫn giữ khóa ngoại tới category,
     * nên xóa category sẽ vi phạm ràng buộc nếu chỉ kiểm tra bằng JPQL (bị @SQLRestriction lọc).
     */
    @Query(value = "SELECT EXISTS (SELECT 1 FROM ingredients WHERE category_id = :categoryId)", nativeQuery = true)
    boolean existsAnyByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Tìm nguyên liệu không phân biệt hoa thường và dấu tiếng Việt, phân trang cursor theo id.
     * Chỉ trả nguyên liệu của hệ thống và của chính {@code userId}.
     * translate() là hàm có sẵn của PostgreSQL (không cần extension): đổi ký tự có dấu thành không dấu
     * theo bảng TextUtil.ACCENT_FROM -> TextUtil.ACCENT_TO.
     * Tham số không dùng thì truyền AppUtil.NIL_UUID / allCategories = true (tránh truyền null cho PostgreSQL).
     */
    @Query("""
            SELECT i FROM Ingredient i
            LEFT JOIN FETCH i.category c
            WHERE i.id > :cursor
              AND (i.owner IS NULL OR i.owner.id = :userId)
              AND (:allCategories = true OR c.id = :categoryId)
              AND lower(function('translate', i.name,
                      :#{T(org.group12.common.utils.TextUtil).ACCENT_FROM},
                      :#{T(org.group12.common.utils.TextUtil).ACCENT_TO})) LIKE :pattern ESCAPE '!'
            ORDER BY i.id ASC
            """)
    List<Ingredient> search(@Param("pattern") String pattern,
                            @Param("allCategories") boolean allCategories,
                            @Param("categoryId") UUID categoryId,
                            @Param("userId") UUID userId,
                            @Param("cursor") UUID cursor,
                            Pageable pageable);
}