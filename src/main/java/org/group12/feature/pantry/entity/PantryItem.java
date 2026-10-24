package org.group12.feature.pantry.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.SoftDeletableEntity;
import org.group12.common.enums.Unit;
import org.group12.feature.auth.entity.User;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.pantry.enums.StorageLocation;
import org.group12.feature.sysc.entity.SyncStatus;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pantry_items")
@SQLDelete(sql = "UPDATE pantry_items SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// nơi lưu trữ 1 nguyên liệu mà người dùng đang có
public class PantryItem extends SoftDeletableEntity {
    @Id
    private UUID id;
    private Double quantity;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    @Enumerated(EnumType.STRING)
    private StorageLocation location;
    private LocalDate expiryDate;
    private String photoUri;
    private String publicImgId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sync_status_id")
    private SyncStatus syncStatus;
}