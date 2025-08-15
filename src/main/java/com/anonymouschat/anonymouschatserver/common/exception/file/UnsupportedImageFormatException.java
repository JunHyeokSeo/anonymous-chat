package com.anonymouschat.anonymouschatserver.common.exception.file;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class UnsupportedImageFormatException extends AbstractCustomException {
    public UnsupportedImageFormatException(ErrorCode errorCode) {
        super(errorCode);
    }
}