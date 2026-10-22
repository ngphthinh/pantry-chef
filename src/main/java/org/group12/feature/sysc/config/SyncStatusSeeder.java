package org.group12.feature.sysc.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.utils.AppUtil;
import org.group12.feature.sysc.entity.SyncStatus;
import org.group12.feature.sysc.repository.SyncStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Seed bảng tra cứu {@code sync_statuses}.
 * Idempotent: chỉ thêm các code còn thiếu.
 */
@Slf4j
@Order(10)
@Component
@RequiredArgsConstructor
public class SyncStatusSeeder implements CommandLineRunner {

    private final SyncStatusRepository syncStatusRepository;
    private final AppUtil appUtil;

    private static final Map<String, String> STATUSES = new LinkedHashMap<>();

    static {
        STATUSES.put("PENDING", "Chờ đồng bộ");
        STATUSES.put("SYNCING", "Đang đồng bộ");
        STATUSES.put("SYNCED", "Đã đồng bộ");
        STATUSES.put("FAILED", "Đồng bộ thất bại");
        STATUSES.put("CONFLICT", "Xung đột dữ liệu");
    }

    @Override
    public void run(String... args) {
        Set<String> existing = syncStatusRepository.findAllByCodeIn(STATUSES.keySet())
                .stream()
                .map(SyncStatus::getCode)
                .collect(Collectors.toSet());

        List<SyncStatus> missing = STATUSES.entrySet().stream()
                .filter(e -> !existing.contains(e.getKey()))
                .map(e -> SyncStatus.builder()
                        .id(appUtil.generateUUID())
                        .code(e.getKey())
                        .name(e.getValue())
                        .build())
                .toList();

        if (!missing.isEmpty()) {
            syncStatusRepository.saveAll(missing);
            log.info("Seeded {} sync statuses", missing.size());
        }
    }
}
