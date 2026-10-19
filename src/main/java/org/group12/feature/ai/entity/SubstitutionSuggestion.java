package org.group12.feature.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.BaseEntity;
import org.group12.feature.recipe.entity.RecipeIngredient;

import java.util.UUID;

@Entity
@Table(name = "substitution_suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubstitutionSuggestion extends BaseEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String substituteName;
    private String ratio;
    @Column(columnDefinition = "TEXT")
    private String note;
    private Double confidence;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_ingredient_id")
    private RecipeIngredient recipeIngredient;
}