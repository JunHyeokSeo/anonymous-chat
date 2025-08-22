package com.anonymouschat.anonymouschatserver.common.exception.storage;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class TokenSerializationException extends AbstractCustomException {

	public TokenSerializationException(ErrorCode errorCode) {
		super(errorCode);
	}

	public TokenSerializationException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}