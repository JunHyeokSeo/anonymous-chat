package com.anonymouschat.anonymouschatserver.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@NoArgsConstructor
public class ChatRoomExit {

	@Column(name = "user1_exited", nullable = false)
	private boolean user1Exited = false;

	@Column(name = "user2_exited", nullable = false)
	private boolean user2Exited = false;

	@Column(name = "user1_exited_at")
	private LocalDateTime user1ExitedAt;

	@Column(name = "user2_exited_at")
	private LocalDateTime user2ExitedAt;

	public void exitByUser(Long userId, Long user1Id, Long user2Id) {
		if (userId.equals(user1Id)) {
			user1Exited = true;
			user1ExitedAt = LocalDateTime.now();
		} else if (userId.equals(user2Id)) {
			user2Exited = true;
			user2ExitedAt = LocalDateTime.now();
		}
	}

	public void returnByUser(Long userId, Long user1Id, Long user2Id) {
		if (userId.equals(user1Id)) {
			user1Exited = false;
		} else if (userId.equals(user2Id)) {
			user2Exited = false;
		}
	}

	public LocalDateTime getExitTime(Long userId, Long user1Id, Long user2Id) {
		if (userId.equals(user1Id)) return user1ExitedAt;
		else if (userId.equals(user2Id)) return user2ExitedAt;
		else throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
	}

	public boolean bothExited() {
		return user1Exited && user2Exited;
	}
}
