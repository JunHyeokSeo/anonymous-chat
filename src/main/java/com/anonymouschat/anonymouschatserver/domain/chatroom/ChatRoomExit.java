package com.anonymouschat.anonymouschatserver.domain.chatroom;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ChatRoomExit {

	@Column(name = "user1_exited", nullable = false)
	private boolean user1Exited = false;

	@Column(name = "user2_exited", nullable = false)
	private boolean user2Exited = false;

	// 도메인 메서드 (예시)
	public void exitByUser1() {
		this.user1Exited = true;
	}

	public void exitByUser2() {
		this.user2Exited = true;
	}

	public boolean bothExited() {
		return user1Exited && user2Exited;
	}
}
