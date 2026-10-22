package org.group12.feature.sysc.repository;

import org.group12.feature.sysc.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SyncStatusRepository extends JpaRepository<SyncStatus, UUID> {
    Optional<SyncStatus> findByCode(String code);

    boolean existsByCode(String code);

    List<SyncStatus> findAllByCodeIn(Collection<String> codes);
}
