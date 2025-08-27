package com.anonymouschat.anonymouschatserver.common.exception.auth;

import org.springframework.security.authentication.BadCredentialsException;

public class UnsupportedAlgorithmAuthenticationException extends BadCredentialsException {
	public UnsupportedAlgorithmAuthenticationException(String msg) {
		super(msg);
	}
	public UnsupportedAlgorithmAuthenticationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
