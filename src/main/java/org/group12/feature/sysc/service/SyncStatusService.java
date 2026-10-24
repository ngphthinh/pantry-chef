package org.group12.feature.sysc.service;

import lombok.RequiredArgsConstructor;
import org.group12.feature.sysc.dto.response.SyncStatusResponse;
import org.group12.feature.sysc.mapper.SyncStatusMapper;
import org.group12.feature.sysc.repository.SyncStatusRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncStatusService {

    private final SyncStatusRepository syncStatusRepository;
    private final SyncStatusMapper syncStatusMapper;

    @Transactional(readOnly = true)
    public List<SyncStatusResponse> getStatuses() {
        return syncStatusMapper.toSyncStatusResponses(syncStatusRepository.findAll(Sort.by("id")));
    }
}
