package org.group12.feature.cookbook.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.SoftDeletableEntity;
import org.group12.feature.auth.entity.User;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.sysc.entity.SyncStatus;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "saved_recipes")
@SQLDelete(sql = "UPDATE saved_recipes SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedRecipe extends SoftDeletableEntity {
    @Id
    private UUID id;
    private Integer rating;
    @Column(columnDefinition = "TEXT")
    private String note;
    private boolean favorite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sync_status_id")
    private SyncStatus syncStatus;
}