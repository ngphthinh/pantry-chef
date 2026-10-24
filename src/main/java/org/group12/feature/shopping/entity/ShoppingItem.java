package org.group12.feature.shopping.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.SoftDeletableEntity;
import org.group12.common.enums.Unit;
import org.group12.feature.shopping.enums.ShoppingItemStatus;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "shopping_items")
@SQLDelete(sql = "UPDATE shopping_items SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingItem extends SoftDeletableEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;
    private Double quantity;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShoppingItemStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shopping_list_id")
    private ShoppingList shoppingList;
}