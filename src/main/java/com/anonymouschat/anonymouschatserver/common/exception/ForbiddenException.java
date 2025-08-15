package com.anonymouschat.anonymouschatserver.common.exception;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;

public class ForbiddenException extends AbstractCustomException {
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
