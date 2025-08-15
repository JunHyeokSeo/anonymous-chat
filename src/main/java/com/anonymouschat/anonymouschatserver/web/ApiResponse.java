package com.anonymouschat.anonymouschatserver.web;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.code.SuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public record ApiResponse<T>(
		String code,
		String message,
		T data
) {

	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>(SuccessCode.SUCCESS.getCode(), SuccessCode.SUCCESS.getMessage(), null);
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(SuccessCode.SUCCESS.getCode(), SuccessCode.SUCCESS.getMessage(), data);
	}

	public static <T> ApiResponse<T> success(SuccessCode code) {
		return new ApiResponse<>(code.getCode(), code.getMessage(), null);
	}

	public static <T> ApiResponse<T> success(SuccessCode code, T data) {
		return new ApiResponse<>(code.getCode(), code.getMessage(), data);
	}

	public static <T> ApiResponse<T> error(String code, String message) {
		return new ApiResponse<>(code, message, null);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
		return new ApiResponse<>(errorCode.name(), message, null);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.name(), errorCode.name(), null);
	}

	/**
	 * HttpServletResponse에 JSON 형태로 ApiResponse를 작성
	 */
	public static void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");
		ApiResponse<?> body = ApiResponse.error(errorCode, message);
		new ObjectMapper().writeValue(response.getWriter(), body);
	}
}