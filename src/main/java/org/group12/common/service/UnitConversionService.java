package org.group12.common.service;

import org.springframework.stereotype.Service;
import org.group12.common.dto.response.UnitConvertResponse;
import org.group12.common.enums.Unit;
import org.group12.common.exception.UnitIncompatibleException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Service
public class UnitConversionService {
    private enum Dimension { MASS, VOLUME, COUNT }

    private record Spec(Dimension dimension, BigDecimal toBase) {}

    private static final BigDecimal TSP_ML = new BigDecimal("4.92892159375");

    private static final Map<Unit, Spec> SPECS = new EnumMap<>(Unit.class);
    static {
        SPECS.put(Unit.MG,    new Spec(Dimension.MASS, new BigDecimal("0.001")));
        SPECS.put(Unit.G,     new Spec(Dimension.MASS, BigDecimal.ONE));
        SPECS.put(Unit.KG,    new Spec(Dimension.MASS, new BigDecimal("1000")));
        SPECS.put(Unit.ML,    new Spec(Dimension.VOLUME, BigDecimal.ONE));
        SPECS.put(Unit.L,     new Spec(Dimension.VOLUME, new BigDecimal("1000")));
        SPECS.put(Unit.TSP,   new Spec(Dimension.VOLUME, TSP_ML));
        SPECS.put(Unit.TBSP,  new Spec(Dimension.VOLUME, TSP_ML.multiply(BigDecimal.valueOf(3))));
        SPECS.put(Unit.CUP,   new Spec(Dimension.VOLUME, TSP_ML.multiply(BigDecimal.valueOf(48))));
        SPECS.put(Unit.PINCH, new Spec(Dimension.VOLUME,
                TSP_ML.divide(BigDecimal.valueOf(16), MathContext.DECIMAL64)));
        SPECS.put(Unit.PCS,   new Spec(Dimension.COUNT, BigDecimal.ONE));
    }

    public UnitConvertResponse convert(BigDecimal value, Unit from, Unit to) {
        Spec f = SPECS.get(from);
        Spec t = SPECS.get(to);
        if (f.dimension() != t.dimension()) {
            throw new UnitIncompatibleException();
        }
        BigDecimal result = value.multiply(f.toBase())
                .divide(t.toBase(), MathContext.DECIMAL64)
                .setScale(4, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return new UnitConvertResponse(value, from, to, result);
    }
}
