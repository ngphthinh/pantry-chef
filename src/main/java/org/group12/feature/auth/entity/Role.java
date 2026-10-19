package org.group12.feature.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.BaseEntity;
import org.group12.feature.auth.enums.RoleName;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {
    @Id
    private UUID id;
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    // Role 1 -- n User
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
}
