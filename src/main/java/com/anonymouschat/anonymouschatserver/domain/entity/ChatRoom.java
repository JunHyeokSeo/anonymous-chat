package com.anonymouschat.anonymouschatserver.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(
		name = "chat_room",
		indexes = {
				@Index(name = "idx_chat_room_user1_active_updated", columnList = "user1_id, is_active, updated_at"),
				@Index(name = "idx_chat_room_user2_active_updated", columnList = "user2_id, is_active, updated_at"),
				@Index(name = "ux_chat_room_pair_active", columnList = "pair_left_id, pair_right_id, is_active", unique = true)
		}
)
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

	@Getter
	@Column(name = "pair_left_id", nullable = false)
	private Long pairLeftId;

	@Getter
	@Column(name = "pair_right_id", nullable = false)
	private Long pairRightId;

	@Getter
	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@Embedded
	@Getter
	private ChatRoomExit exit;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	private void prePersist() {
		long a = user1.getId();
		long b = user2.getId();
		this.pairLeftId = Math.min(a, b);
		this.pairRightId = Math.max(a, b);
		touch();
	}

	public void touch() { this.updatedAt = LocalDateTime.now(); }

	//채팅 시작 시, 활성화
	public void returnBy(Long userId) {
		if (!isParticipant(userId)) throw new IllegalStateException("채팅방 참여자가 아닙니다.");
		exit.returnByUser(userId, user1.getId(), user2.getId());
	}

	public void exitBy(Long userId) {
		if (!isParticipant(userId)) throw new IllegalStateException("채팅방 참여자가 아닙니다.");
		exit.exitByUser(userId, user1.getId(), user2.getId());
		updateActiveIfBothExited();
	}

	public LocalDateTime getLastExitedAt(Long userId) {
		return this.exit.getExitTime(userId, user1.getId(), user2.getId());
	}

	public void validateParticipant(Long userId) {
		if (!isParticipant(userId))
			throw new IllegalStateException("채팅방 참여자가 아닙니다.");
	}

	public boolean isParticipant(Long userId) {
		return user1.getId().equals(userId) || user2.getId().equals(userId);
	}

	public void activate() { // 정책상 활성화 지점에서 호출
		if (!this.isActive) this.isActive = true;
	}

	private void updateActiveIfBothExited() {
		if (exit.bothExited()) this.isActive = false;
	}

	@Builder
	public ChatRoom(User user1, User user2) {
		this.user1 = user1;
		this.user2 = user2;
		this.isActive = false; // 생성 직후 비활성(정책 유지)
		this.exit = new ChatRoomExit();
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
}
