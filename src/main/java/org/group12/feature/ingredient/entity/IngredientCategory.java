package org.group12.feature.ingredient.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Table(name = "ingredient_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientCategory extends BaseEntity {
    @Id
    private UUID id;
    @Column(unique = true, nullable = false)
    private String code;
    @Column(nullable = false)
    private String name;

    // 0 = nhóm bình thường (sắp theo id giảm dần, mới nhất lên đầu); 1 = nhóm ghim cuối danh sách ("Khác").
    @ColumnDefault("0")
    @Column(nullable = false)
    private int sortOrder;
}