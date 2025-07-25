package com.anonymouschat.anonymouschatserver.domain.chatroom.entity;

import com.anonymouschat.anonymouschatserver.domain.chatroom.type.ChatRoomStatus;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "chat_room")
public class ChatRoom {

	@Getter
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Getter
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user1_id", nullable = false)
	private User user1;

	@Getter
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user2_id", nullable = false)
	private User user2;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ChatRoomStatus status;

	@Embedded
	@Getter
	private ChatRoomExit exit;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public void touch() {
		this.updatedAt = LocalDateTime.now();
	}

	public void returnBy(Long userId) {
		if (!isParticipant(userId)) {
			throw new IllegalStateException("채팅방 참여자가 아닙니다.");
		}
		exit.returnByUser(userId, user1.getId(), user2.getId());
	}

	public void exitBy(Long userId) {
		if (!isParticipant(userId)) {
			throw new IllegalStateException("채팅방 참여자가 아닙니다.");
		}
		exit.exitByUser(userId, user1.getId(), user2.getId());
		updateStatusIfBothExited();
	}

	public LocalDateTime getLastExitedAt(Long userId) {
		return this.exit.getExitTime(userId, user1.getId(), user2.getId());
	}

	public void validateParticipant(Long userId) {
		if (!isParticipant(userId))
			throw new IllegalStateException("유저가 속한 채팅방이 아닙니다.");
	}

	public boolean isParticipant(Long userId) {
		return user1.getId().equals(userId) || user2.getId().equals(userId);
	}

	private void updateStatusIfBothExited() {
		if (exit.bothExited()) {
			this.status = ChatRoomStatus.INACTIVE;
		}
	}

	@Builder
	public ChatRoom(User user1, User user2) {
		this.user1 = user1;
		this.user2 = user2;
		this.status = ChatRoomStatus.ACTIVE;
		this.exit = new ChatRoomExit();
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
}
