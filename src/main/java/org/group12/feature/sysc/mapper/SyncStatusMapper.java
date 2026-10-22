package org.group12.feature.sysc.mapper;

import org.group12.feature.sysc.dto.response.SyncStatusResponse;
import org.group12.feature.sysc.entity.SyncStatus;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SyncStatusMapper {
    SyncStatusResponse toResponse(SyncStatus syncStatus);

    List<SyncStatusResponse> toResponses(List<SyncStatus> syncStatuses);
}
