package org.group12.common.service;

import org.group12.common.dto.response.MetaEnumsResponse;
import org.group12.common.enums.Unit;
import org.group12.feature.auth.enums.MeasurementSystem;
import org.group12.feature.auth.enums.ThemeMode;
import org.group12.feature.mealplan.enums.MealType;
import org.group12.feature.pantry.enums.StorageLocation;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.shopping.enums.ShoppingItemStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MetaService {

    // enum cố định lúc chạy, tính một lần dùng lại cho mọi request
    private final List<String> units = names(Unit.class);
    private final MetaEnumsResponse enums = new MetaEnumsResponse(
            units,
            names(MealType.class),
            names(Difficulty.class),
            names(StorageLocation.class),
            names(ShoppingItemStatus.class),
            names(MeasurementSystem.class),
            names(ThemeMode.class));

    public List<String> getUnits() {
        return units;
    }

    public MetaEnumsResponse getEnums() {
        return enums;
    }

    private static <E extends Enum<E>> List<String> names(Class<E> type) {
        return Arrays.stream(type.getEnumConstants()).map(Enum::name).toList();
    }
}