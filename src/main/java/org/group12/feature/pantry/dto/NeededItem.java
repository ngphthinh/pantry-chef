package org.group12.feature.pantry.dto;

import lombok.*;
import org.group12.common.enums.Unit;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeededItem {
    private String displayName;
    private double amount;
    private Unit unit;
}