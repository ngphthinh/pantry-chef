package org.group12.feature.recipe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.BaseEntity;
import org.group12.common.enums.Unit;

import java.util.UUID;

@Entity
@Table(name = "recipe_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeIngredient extends BaseEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;
    private Double amount;
    @Enumerated(EnumType.STRING)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;
}