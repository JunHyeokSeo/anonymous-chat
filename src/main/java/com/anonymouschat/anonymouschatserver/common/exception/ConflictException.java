package com.anonymouschat.anonymouschatserver.common.exception;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;

public class ConflictException extends AbstractCustomException {
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}
