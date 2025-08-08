package com.anonymouschat.anonymouschatserver.infra.web;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
		String errorMessage = ex.getBindingResult().getFieldErrors().stream()
				                      .map(error -> String.format("[%s] %s", error.getField(), error.getDefaultMessage()))
				                      .collect(Collectors.joining(" / "));

		return ResponseEntity.badRequest().body(
				ApiResponse.error(ErrorCode.INVALID_INPUT, errorMessage));
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


