package com.anonymouschat.anonymouschatserver.common.exception.socket;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class UnsupportedMessageTypeException extends AbstractCustomException {
    public UnsupportedMessageTypeException(ErrorCode errorCode) {
        super(errorCode);
    }
}