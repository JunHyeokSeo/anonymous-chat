package com.anonymouschat.anonymouschatserver.common.exception.user;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class CannotBlockSelfException extends AbstractCustomException {
    public CannotBlockSelfException(ErrorCode errorCode) {
        super(errorCode);
    }
}