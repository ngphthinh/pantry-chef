package org.group12.feature.ai.dto.response;

import lombok.*;

// dto
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RecognizedIngredientResponse {
    private String name;        // tên chuẩn hoá, tiếng Việt
    private String nameEn;      // dùng để search recipe API
    private Double confidence;  // 0..1
    private String quantityHint; // vd: "3 quả", có thể null
}
