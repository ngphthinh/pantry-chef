package org.group12.feature.recipe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "cuisines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuisine extends BaseEntity {
    @Id
    private UUID id;
    @Column(unique = true, nullable = false)
    private String code;
    @Column(nullable = false)
    private String name;
}