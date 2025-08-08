package com.anonymouschat.anonymouschatserver.web.socket.dto;

public enum MessageType {
	CHAT,      // 일반 채팅 메시지
	ENTER,     // 채팅방 입장
	READ       // 메시지 읽음 처리
}
