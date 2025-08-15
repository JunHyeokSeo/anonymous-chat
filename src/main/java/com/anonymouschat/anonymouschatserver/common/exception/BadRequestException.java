package com.anonymouschat.anonymouschatserver.common.exception;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;

public class BadRequestException extends AbstractCustomException {
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}