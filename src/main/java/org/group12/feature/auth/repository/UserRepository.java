package org.group12.feature.auth.repository;

import org.group12.feature.auth.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
    select u from User u
    where (cast(:cursor as uuid) is null or u.id > :cursor)
      and (:kw is null
           or lower(u.email) like lower(concat('%', cast(:kw as string), '%'))
           or lower(u.displayName) like lower(concat('%', cast(:kw as string), '%')))
    order by u.id asc
    """)
    List<User> findPage(@Param("cursor") UUID cursor,
                        @Param("kw") String kw,
                        Pageable pageable);
}
