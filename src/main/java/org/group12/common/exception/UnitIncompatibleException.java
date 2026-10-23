package org.group12.common.exception;

public class UnitIncompatibleException extends AppException{
    public UnitIncompatibleException() {
        super(ErrorCode.UNIT_INCOMPATIBLE);
    }
}
