package com.anonymouschat.anonymouschatserver.common.exception.socket;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class MessageValidationException extends AbstractCustomException {
    public MessageValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}