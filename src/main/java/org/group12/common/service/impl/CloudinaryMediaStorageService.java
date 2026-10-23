package org.group12.common.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.dto.response.UploadMediaResponse;
import org.group12.common.enums.MediaType;
import org.group12.common.exception.UploadMediaException;
import org.group12.common.service.MediaStorageService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class CloudinaryMediaStorageService implements MediaStorageService {

    private final Cloudinary cloudinary;

    @Async("taskExecutor")
    @Override
    public CompletableFuture<UploadMediaResponse> uploadImage(
            byte[] data,
            String folder
    ) {
        return upload(data, folder, MediaType.IMAGE);
    }
    @Async("taskExecutor")
    @Override
    public CompletableFuture<UploadMediaResponse> uploadVideo(byte[] data, String folder) {
        return upload(data, folder, MediaType.VIDEO);
    }



    @Async
    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.error("Delete media failed", e);
            throw new UploadMediaException();
        }
    }


    @Override
    public CompletableFuture<UploadMediaResponse> upload(
            byte[] data,
            String folder,
            MediaType mediaType
    ) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    data,
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", mediaType.name().toLowerCase()
                    )
            );

            return CompletableFuture.completedFuture(
                    UploadMediaResponse.builder()
                            .url(uploadResult.get("secure_url").toString())
                            .publicId(uploadResult.get("public_id").toString())
                            .mediaType(mediaType)
                            .build()
            );

        } catch (IOException e) {
            log.error("Upload media failed", e);
            return CompletableFuture.failedFuture(
                    new UploadMediaException()
            );
        }
    }

    @Override
    public UploadMediaResponse uploadSync(MultipartFile file, String folder, MediaType mediaType) {
            try {
                return upload(file.getBytes(), folder, mediaType).join();
            } catch (IOException e) {
                log.error("Upload media failed", e);
                throw new UploadMediaException();
            }
    }
}