package org.group12.common.stats.dto.response;

public record UserStatsResponse(
        long savedRecipeCount,
        long collectionCount,
        long cookedCount,
        long wastedItemCount) {
}
