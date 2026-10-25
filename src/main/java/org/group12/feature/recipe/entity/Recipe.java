package org.group12.feature.recipe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.SoftDeletableEntity;
import org.group12.feature.auth.entity.User;
import org.group12.feature.recipe.enums.Difficulty;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recipes")
@SQLDelete(sql = "UPDATE recipes SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe extends SoftDeletableEntity {
    @Id
    private UUID id;
    @Column(unique = true)
    private String externalId;
    @Column(nullable = false)
    private String title;
    @Column(length = 1000)
    private String imageUrl;
    private String publicImgId; // public id trên Cloudinary, chỉ có với ảnh do người dùng upload
    private Integer servings;
    private Integer readyInMinutes;
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    @Column(length = 1000)
    private String sourceUrl;
    private boolean custom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuisine_id")
    private Cuisine cuisine;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner; // chỉ có khi custom = true

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<RecipeStep> steps = new ArrayList<>();
}