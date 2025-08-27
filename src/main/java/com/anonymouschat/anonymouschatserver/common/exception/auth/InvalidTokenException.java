package com.anonymouschat.anonymouschatserver.common.exception.auth;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class InvalidTokenException extends AbstractCustomException {
    public InvalidTokenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidTokenException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
