package com.anonymouschat.anonymouschatserver.presentation.socket.dto;

/**
 * WebSocket 메시지의 타입을 정의하는 열거형입니다.
 * 각 타입은 클라이언트와 서버 간의 특정 상호작용을 나타냅니다.
 */
public enum MessageType {
	CHAT,      // 일반 채팅 메시지
	ENTER,     // 채팅방 입장
	READ  ,    // 메시지 읽음 처리
	LEAVE      // 채팅방 퇴장
}
