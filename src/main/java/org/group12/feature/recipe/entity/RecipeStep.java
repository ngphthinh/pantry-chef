package org.group12.feature.recipe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "recipe_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeStep extends BaseEntity {
    @Id
    private UUID id;
    @Column(name = "step_order", nullable = false)
    private int stepOrder;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;
    private Integer timerSeconds;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

}