package org.group12.common.exception;

public class UploadMediaException extends AppException {
    public UploadMediaException() {
        super(ErrorCode.UPLOAD_MEDIA_FAILED);
    }
}
