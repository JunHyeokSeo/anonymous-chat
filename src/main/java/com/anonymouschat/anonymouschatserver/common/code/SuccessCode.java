package com.anonymouschat.anonymouschatserver.common.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode {

    // 공통
    SUCCESS(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),

    // 사용자 관련
    USER_REGISTERED(HttpStatus.CREATED, "사용자가 성공적으로 등록되었습니다."),
    USER_LOGGED_OUT(HttpStatus.OK, "로그아웃이 완료되었습니다."),
    USER_PROFILE_UPDATED(HttpStatus.OK, "사용자 프로필이 수정되었습니다."),
	USER_LIST_FETCHED(HttpStatus.OK, "사용자 목록이 조회되었습니다."),

    // 이미지
    IMAGE_UPLOADED(HttpStatus.CREATED, "이미지가 업로드되었습니다."),
    IMAGE_DELETED(HttpStatus.NO_CONTENT, "이미지가 삭제되었습니다."),

    // 채팅방
    CHATROOM_CREATED(HttpStatus.CREATED, "채팅방이 생성되었습니다."),
    CHATROOM_EXITED(HttpStatus.OK, "채팅방을 나갔습니다."),

    // 메시지
    MESSAGE_SENT(HttpStatus.CREATED, "메시지가 전송되었습니다."),
    MESSAGE_LIST_FETCHED(HttpStatus.OK, "메시지 목록이 조회되었습니다."),

    // 차단
    USER_BLOCKED(HttpStatus.CREATED, "사용자가 차단되었습니다."),
    USER_UNBLOCKED(HttpStatus.NO_CONTENT, "차단이 해제되었습니다.");

    private final HttpStatus status;
    private final String message;

    SuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

	public String getCode() {
        return this.name();
    }
}
