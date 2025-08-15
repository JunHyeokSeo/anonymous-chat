package com.anonymouschat.anonymouschatserver.common.exception;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;

public class InternalServerException extends AbstractCustomException {
    public InternalServerException(ErrorCode errorCode) {
        super(errorCode);
    }
}
