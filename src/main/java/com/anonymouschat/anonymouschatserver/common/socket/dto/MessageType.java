package com.anonymouschat.anonymouschatserver.common.socket.dto;

public enum MessageType {
	MESSAGE,      // 일반 채팅 메시지
	ENTER,     // 채팅방 입장
	LEAVE,     // 채팅방 퇴장
	HEARTBEAT  // 하트비트 처리용
}
