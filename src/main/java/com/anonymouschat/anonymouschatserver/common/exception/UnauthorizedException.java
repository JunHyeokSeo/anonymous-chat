package com.anonymouschat.anonymouschatserver.common.exception;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;

public class UnauthorizedException extends AbstractCustomException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}