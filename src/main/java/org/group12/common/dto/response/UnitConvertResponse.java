package org.group12.common.dto.response;

import org.group12.common.enums.Unit;
import java.math.BigDecimal;
public record UnitConvertResponse(
        BigDecimal value, Unit from, Unit to, BigDecimal result
) {
}
