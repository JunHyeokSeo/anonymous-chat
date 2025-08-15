package com.anonymouschat.anonymouschatserver.common.exception;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;

public class NotFoundException extends AbstractCustomException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
