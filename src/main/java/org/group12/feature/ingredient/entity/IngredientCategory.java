package org.group12.feature.ingredient.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.BaseEntity;
import org.group12.feature.auth.entity.User;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

/**
 * Danh mục nguyên liệu. owner = NULL là danh mục của hệ thống (ai cũng thấy, chỉ ADMIN sửa);
 * có giá trị là danh mục do người dùng tự tạo (chỉ chủ sở hữu thấy và sửa).
 * Mã (code) là duy nhất theo từng chủ sở hữu; với danh mục hệ thống (owner NULL) cần thêm
 * unique index riêng, xem migration.sql vì unique constraint của Postgres coi các NULL là khác nhau.
 */
@Entity
@Table(
        name = "ingredient_categories",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_ingredient_categories_code_owner",
                columnNames = {"code", "owner_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientCategory extends BaseEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private String name;

    // 0 = nhóm bình thường (sắp theo id giảm dần, mới nhất lên đầu); 1 = nhóm ghim cuối danh sách ("Khác").
    @ColumnDefault("0")
    @Column(nullable = false)
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;
}