package com.anonymouschat.anonymouschatserver.common.exception;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import lombok.Getter;

@Getter
public abstract class AbstractCustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public AbstractCustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AbstractCustomException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}