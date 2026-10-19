package org.group12.feature.shopping.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.SoftDeletableEntity;
import org.group12.feature.auth.entity.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shopping_lists")
@SQLDelete(sql = "UPDATE shopping_lists SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingList extends SoftDeletableEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShoppingItem> items = new ArrayList<>();
}