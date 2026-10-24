package org.group12.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserStatsResponse {
    private long savedRecipeCount;
    private long collectionCount;
    private long cookedCount;
    private long wastedItemCount;

}