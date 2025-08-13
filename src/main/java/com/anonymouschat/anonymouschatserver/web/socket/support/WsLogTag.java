package com.anonymouschat.anonymouschatserver.web.socket.support;

/**
 * WebSocket 관련 로그 메시지에 사용되는 태그를 정의하는 유틸리티 클래스입니다.
 * 로그 메시지의 가독성과 분류를 돕습니다.
 */
public final class WsLogTag {
	private WsLogTag() {}

	/** 시스템 관련 로그 태그 */
	public static String sys()  { return "[WS][SYS] "; }
	/** 에러 관련 로그 태그 */
	public static String err()  { return "[WS][ERR] "; }
	/** 채팅 메시지 관련 로그 태그 */
	public static String chat() { return "[WS][CHAT] "; }
	/** 채팅방 입장 관련 로그 태그 */
	public static String enter(){ return "[WS][ENTER] "; }
	/** 메시지 읽음 처리 관련 로그 태그 */
	public static String read() { return "[WS][READ] "; }
	/** 채팅방 퇴장 관련 로그 태그 */
	public static String leave() { return "[WS][LEAVE] "; }
	/** Pong 메시지 관련 로그 태그 */
	public static String pong() { return "[WS][PONG] "; }
	/** 브로드캐스트 관련 로그 태그 */
	public static String bc()   { return "[WS][BC] "; }
	/** 정책 위반 관련 로그 태그 */
	public static String policy()   { return "[WS][POLICY] "; }
}
