package com.anonymouschat.anonymouschatserver.common.exception.file;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class FileUploadException extends AbstractCustomException {
    public FileUploadException(ErrorCode errorCode) {
        super(errorCode);
    }
}
