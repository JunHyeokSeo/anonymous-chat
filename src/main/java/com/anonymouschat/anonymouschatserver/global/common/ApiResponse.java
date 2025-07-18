package com.anonymouschat.anonymouschatserver.global.common;

public record ApiResponse<T>(
		String code,
		String message,
		T data
) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>("SUCCESS", "요청이 성공했습니다.", data);
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>("SUCCESS", message, data);
	}

	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>("SUCCESS", "요청이 성공했습니다.", null);
	}

	public static <T> ApiResponse<T> error(String code, String message) {
		return new ApiResponse<>(code, message, null);
	}
}
