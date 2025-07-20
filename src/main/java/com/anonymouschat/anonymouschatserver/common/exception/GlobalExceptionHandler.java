package com.anonymouschat.anonymouschatserver.common.exception;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.response.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				                 .findFirst()
				                 .map(field -> field.getField() + " " + field.getDefaultMessage())
				                 .orElse("Validation error");

		return ResponseEntity.badRequest()
				       .body(ApiResponse.error(ErrorCode.INVALID_INPUT, message));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				       .body(ApiResponse.error(ErrorCode.FORBIDDEN, "접근이 거부되었습니다."));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		return ResponseEntity.badRequest()
				       .body(ApiResponse.error(ErrorCode.INVALID_INPUT, "잘못된 요청 파라미터 형식입니다."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleAllUnhandled(Exception e) {
		return ResponseEntity.internalServerError()
				       .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "서버 에러가 발생했습니다."));
	}
}


