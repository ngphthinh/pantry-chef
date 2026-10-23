package org.group12.common.dto.response;

import java.util.List;

public record MetaEnumsResponse(
        List<String> unit,
        List<String> mealType,
        List<String> difficulty,
        List<String> storageLocation,
        List<String> shoppingItemStatus,
        List<String> measurementSystem,
        List<String> themeMode
) {
}
