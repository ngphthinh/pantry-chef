package org.group12.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.enums.MediaType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UploadMediaResponse {
    private String publicId;
    private String url;
    private MediaType mediaType;

}