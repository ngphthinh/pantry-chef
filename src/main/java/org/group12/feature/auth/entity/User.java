package org.group12.feature.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.group12.common.entity.SoftDeletableEntity;
import org.group12.feature.auth.enums.MeasurementSystem;
import org.group12.feature.auth.enums.ThemeMode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends SoftDeletableEntity {
    @Id
    private UUID id;
    @Column(unique = true)
    private String email;
    private String password;
    private String displayName;
    private String avatarUri;
    private String publicAvtId;
    @Enumerated(EnumType.STRING)
    private MeasurementSystem measurementSystem;
    @Enumerated(EnumType.STRING)
    private ThemeMode themeMode;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
