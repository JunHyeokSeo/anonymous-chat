package com.anonymouschat.anonymouschatserver.common.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

	// 공통
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다."),
	TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 파라미터 타입이 잘못되었습니다."),
	MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
	CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "요청 값 제약조건을 위반했습니다."),

	// HTTP
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP Method 입니다."),

	// 인증/인가
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
	OAUTH_PROVIDER_ERROR(HttpStatus.UNAUTHORIZED, "OAuth 제공자와 통신 중 오류가 발생했습니다."),
	INVALID_PRINCIPAL_STATE(HttpStatus.INTERNAL_SERVER_ERROR, "서버 인증 객체 생성에 실패했습니다."),
	INVALID_OAUTH_TOKEN_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 OAuth 토큰 타입입니다."),
	UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth Provider 입니다."),
	TOKEN_THEFT_DETECTED(HttpStatus.UNAUTHORIZED, "토큰 탈취 시도가 감지되었습니다. 다시 로그인해주세요."),

	// 유저 관련
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
	USER_GUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "임시 OAuth 유저를 찾을 수 없습니다."),
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
	ALREADY_REGISTERED_USER(HttpStatus.CONFLICT, "이미 등록된 사용자입니다."),
	CANNOT_BLOCK_SELF(HttpStatus.BAD_REQUEST, "자기 자신을 차단할 수 없습니다."),
	PROFILE_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "프로필 이미지는 최대 3장까지만 등록할 수 있습니다."),
	BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "차단 정보를 찾을 수 없습니다."),

	// 채팅 관련
	CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
	CHAT_ROOM_CLOSED(HttpStatus.CONFLICT, "종료된 대화방입니다. 새 대화방을 사용하세요."),
	NOT_CHAT_ROOM_MEMBER(HttpStatus.FORBIDDEN, "채팅방 참여자가 아닙니다."),
	CHAT_ROOM_CONCURRENCY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "동시성 충돌 후 활성 방 조회 실패"),

	// 파일 관련
	FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
	FILE_IS_EMPTY(HttpStatus.BAD_REQUEST, "파일이 비어 있습니다."),
	UNSUPPORTED_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다."),
	INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "잘못된 파일 이름입니다."),

	// 소켓/메시지 관련
	UNSUPPORTED_MESSAGE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 메시지 타입입니다."),
	PAYLOAD_REQUIRED(HttpStatus.BAD_REQUEST, "메시지 페이로드가 필요합니다."),
	ROOM_ID_REQUIRED(HttpStatus.BAD_REQUEST, "채팅방 ID가 필요합니다."),
	MESSAGE_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "메시지 타입이 필요합니다."),
	CONTENT_REQUIRED_FOR_CHAT(HttpStatus.BAD_REQUEST, "CHAT 타입 메시지는 내용이 필요합니다."),
	CONTENT_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "메시지 길이가 제한을 초과했습니다."),
	CONTENT_SHOULD_BE_EMPTY(HttpStatus.BAD_REQUEST, "해당 메시지 타입은 내용이 비어있어야 합니다."),
	SENDER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "송신자 ID가 필요합니다."),
	TIMESTAMP_REQUIRED(HttpStatus.BAD_REQUEST, "타임스탬프가 필요합니다.");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	public String getCode() {
		return this.name();
	}
}
