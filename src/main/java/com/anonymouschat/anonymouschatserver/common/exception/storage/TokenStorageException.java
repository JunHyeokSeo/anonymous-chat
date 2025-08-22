package com.anonymouschat.anonymouschatserver.common.exception.storage;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class TokenStorageException extends AbstractCustomException {

	public TokenStorageException(ErrorCode errorCode) {
		super(errorCode);
	}

	public TokenStorageException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}