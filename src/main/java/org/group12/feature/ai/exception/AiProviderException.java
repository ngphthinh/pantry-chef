package org.group12.feature.ai.exception;

import org.group12.common.exception.AppException;

public class AiProviderException extends AppException {

    public AiProviderException() {
        super(AiErrorCode.AI_PROVIDER_ERROR);
    }
}