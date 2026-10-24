package org.group12.feature.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Response body for ingredient substitution")
public class SubstitutionResponse {
    @Schema(description = "Name of the substitute ingredient", example = "Butter")
    private String name;

    @Schema(description = "The ratio of the substitute ingredient to the original ingredient", example = "1:1")
    private String ratio;       // vd: "1:1", "3/4 lượng ban đầu"
    @Schema(description = "Notes on how the substitute ingredient affects flavor/texture", example = "May alter the flavor slightly")
    private String note;        // ghi chú ảnh hưởng hương vị/kết cấu
    @Schema(description = "Confidence level of the substitution suggestion, ranging from 0 to 1", example = "0.85")
    private Double confidence;  // 0..1
}