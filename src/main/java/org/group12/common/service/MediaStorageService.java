package org.group12.common.service;


import org.group12.common.dto.response.UploadMediaResponse;
import org.group12.common.enums.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface MediaStorageService {

    CompletableFuture<UploadMediaResponse> uploadImage(byte[] data, String folder);

    CompletableFuture<UploadMediaResponse> uploadVideo(byte[] data, String folder);

    void delete(String publicId);

    CompletableFuture<UploadMediaResponse> upload(
            byte[] data,
            String folder,
            MediaType mediaType
    );

    UploadMediaResponse uploadSync(MultipartFile file, String folder, MediaType mediaType);
}
