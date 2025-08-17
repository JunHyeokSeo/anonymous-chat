package com.anonymouschat.anonymouschatserver.web;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.code.SuccessCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

import java.io.IOException;

@Getter
public class ApiResponse<T> {

	private final boolean success;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final T data;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final ErrorBody error;

	private ApiResponse(boolean success, T data, ErrorBody error) {
		this.success = success;
		this.data = data;
		this.error = error;
	}

	/**
	 * 성공 응답 생성
	 */
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	/**
	 * 에러 응답 생성 - ErrorCode 기반
	 */
	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return new ApiResponse<>(false, null, new ErrorBody(errorCode));
	}

	/**
	 * 에러 응답 생성 - 메시지 override 가능
	 */
	public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage) {
		return new ApiResponse<>(false, null, new ErrorBody(errorCode, customMessage));
	}

	@Getter
	public static class ErrorBody {
		private final String code;
		private final String message;

		public ErrorBody(ErrorCode errorCode) {
			this.code = errorCode.name();
			this.message = errorCode.getMessage();
		}

		public ErrorBody(ErrorCode errorCode, String customMessage) {
			this.code = errorCode.name();
			this.message = customMessage;
		}
	}

	/**
	 * HttpServletResponse에 JSON 형태로 ApiResponse를 작성
	 */
	public static void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
		response.setStatus(errorCode.getStatus().value());
		response.setContentType("application/json;charset=UTF-8");
		ApiResponse<?> body = ApiResponse.error(errorCode);
		new ObjectMapper().writeValue(response.getWriter(), body);
	}
}