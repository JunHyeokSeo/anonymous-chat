package com.anonymouschat.anonymouschatserver.common.exception.user;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class DuplicateNicknameException extends AbstractCustomException {
    public DuplicateNicknameException(ErrorCode errorCode) {
        super(errorCode);
    }
}