package com.anonymouschat.anonymouschatserver.common.exception.storage;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class TokenDeserializationException extends AbstractCustomException {

	public TokenDeserializationException(ErrorCode errorCode) {
		super(errorCode);
	}

	public TokenDeserializationException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}