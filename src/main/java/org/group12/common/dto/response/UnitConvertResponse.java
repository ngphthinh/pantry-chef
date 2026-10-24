package org.group12.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.enums.Unit;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UnitConvertResponse {
    private BigDecimal value;
    private Unit from;
    private Unit to;
    private BigDecimal result;

}
