package org.group12.feature.mealplan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.SoftDeletableEntity;
import org.group12.feature.mealplan.enums.MealType;
import org.group12.feature.recipe.entity.Recipe;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "meal_plan_entries")
@SQLDelete(sql = "UPDATE meal_plan_entries SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlanEntry extends SoftDeletableEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType mealType;
    private Integer servings;
    private boolean cooked;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_plan_id")
    private MealPlan mealPlan;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "recipe_id", nullable = true)
    private Recipe recipe;
}