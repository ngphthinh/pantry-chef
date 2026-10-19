package org.group12.feature.ingredient.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.BaseEntity;
import org.group12.feature.auth.entity.User;

import java.util.UUID;

@Entity
@Table(name = "excluded_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcludedIngredient extends BaseEntity {
    @Id
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}