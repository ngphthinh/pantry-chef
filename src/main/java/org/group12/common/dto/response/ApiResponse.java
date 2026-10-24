package org.group12.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    @Builder.Default
    private String code = "SUCCESS";

    @Builder.Default
    private String message = "Request processed successfully";

    private T data;

}