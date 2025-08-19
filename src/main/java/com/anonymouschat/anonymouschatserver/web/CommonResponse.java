package com.anonymouschat.anonymouschatserver.web;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

import java.io.IOException;

@Getter
public class CommonResponse<T> {

	@Schema(description = "성공 여부", example = "true")
	private final boolean success;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Schema(description = "응답 데이터", nullable = true)
	private final T data;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Schema(description = "에러 정보", nullable = true)
	private final ErrorBody error;

	private CommonResponse(boolean success, T data, ErrorBody error) {
		this.success = success;
		this.data = data;
		this.error = error;
	}

	/**
	 * 성공 응답 생성
	 */
	public static <T> CommonResponse<T> success(T data) {
		return new CommonResponse<>(true, data, null);
	}

	/**
	 * 에러 응답 생성 - ErrorCode 기반
	 */
	public static <T> CommonResponse<T> error(ErrorCode errorCode) {
		return new CommonResponse<>(false, null, new ErrorBody(errorCode));
	}

	@Getter
	public static class ErrorBody {
		private final String code;
		private final String message;

		public ErrorBody(ErrorCode errorCode) {
			this.code = errorCode.name();
			this.message = errorCode.getMessage();
		}
	}

	/**
	 * HttpServletResponse에 JSON 형태로 ApiResponse를 작성
	 */
	public static void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
		response.setStatus(errorCode.getStatus().value());
		response.setContentType("application/json;charset=UTF-8");
		CommonResponse<?> body = CommonResponse.error(errorCode);
		new ObjectMapper().writeValue(response.getWriter(), body);
	}
}