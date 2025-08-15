package com.anonymouschat.anonymouschatserver.common.exception.user;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class UserNotFoundException extends AbstractCustomException {
    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}