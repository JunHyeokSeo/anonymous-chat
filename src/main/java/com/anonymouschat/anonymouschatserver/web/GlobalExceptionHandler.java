package com.anonymouschat.anonymouschatserver.web;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 커스텀 예외 (AbstractCustomException 기반)
	 */
	@ExceptionHandler(AbstractCustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomException(AbstractCustomException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		log.warn("[CustomException] {} - {}", errorCode.name(), ex.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
				       .body(ApiResponse.error(errorCode));
	}

	/**
	 * {@code @Valid} 바인딩 실패
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
		String errorMessage = ex.getBindingResult().getFieldErrors().stream()
				                      .map(error -> String.format("[%s] %s", error.getField(), error.getDefaultMessage()))
				                      .collect(Collectors.joining(" / "));

		log.warn("[ValidationException] {}", errorMessage);
		ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
		return ResponseEntity.status(errorCode.getStatus())
				       .body(ApiResponse.error(errorCode));
	}


	/**
	 * 파라미터 타입 불일치 (예: int 인데 문자 들어온 경우)
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		log.warn("[TypeMismatchException] {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.TYPE_MISMATCH;
		return ResponseEntity.status(errorCode.getStatus())
				       .body(ApiResponse.error(errorCode));
	}

	/**
	 * 필수 파라미터 누락
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
		log.warn("[MissingParameterException] {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.MISSING_PARAMETER;
		return ResponseEntity.status(errorCode.getStatus())
				       .body(ApiResponse.error(errorCode));
	}

	/**
	 * GET/POST 등 HTTP Method가 잘못된 경우
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		log.warn("[MethodNotSupported] {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
		return ResponseEntity.status(errorCode.getStatus())
				       .body(ApiResponse.error(errorCode));
	}

	/**
	 * {@code @Validated} 검증 실패 (파라미터 단위)
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
		log.warn("[ConstraintViolation] {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.CONSTRAINT_VIOLATION;
		return ResponseEntity.status(errorCode.getStatus())
				       .body(ApiResponse.error(errorCode));
	}

	/**
	 * Spring Security AccessDeniedException
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
		log.warn("[AccessDenied] {}", e.getMessage());
		ErrorCode errorCode = ErrorCode.FORBIDDEN;
		return ResponseEntity.status(errorCode.getStatus())
				       .body(ApiResponse.error(errorCode));
	}

	/**
	 * 그 외 알 수 없는 모든 예외
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnhandled(Exception e) {
		log.error("[UnhandledException] {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(errorCode.getStatus())
				       .body(ApiResponse.error(errorCode));
	}
}
