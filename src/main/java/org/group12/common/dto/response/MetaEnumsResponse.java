package org.group12.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MetaEnumsResponse {
    private List<String> unit;
    private List<String> mealType;
    private List<String> difficulty;
    private List<String> storageLocation;
    private List<String> shoppingItemStatus;
    private List<String> measurementSystem;
    private List<String> themeMode;
}