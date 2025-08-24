package com.anonymouschat.anonymouschatserver.domain.entity;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.chat.NotChatRoomMemberException;
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
	@Getter
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	@Getter
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

	public void validateParticipant(Long userId) {
		if (!isParticipant(userId)) {
			throw new NotChatRoomMemberException(ErrorCode.NOT_CHAT_ROOM_MEMBER);
		}
	}

	/** 단순 조회용(외부에서 읽기 검증 시 사용 가능) */
	public boolean isParticipant(Long userId) {
		return user1.getId().equals(userId) || user2.getId().equals(userId);
	}

	/** 채팅 재개: exit 플래그 복구 + 갱신시간 업데이트 (활성화는 서비스 정책에서) */
	public void returnBy(Long userId) {
		validateParticipant(userId);
		exit.returnByUser(userId, user1.getId(), user2.getId());
		touch(); // ← 목록 정렬/최근 대화 갱신
	}

	/** 사용자 나감: exit 플래그 설정 + 둘 다 나가면 비활성화 + 갱신시간 업데이트 */
	public void exitBy(Long userId) {
		validateParticipant(userId);
		exit.exitByUser(userId, user1.getId(), user2.getId());
		updateActiveIfBothExited();
		touch();
	}

	public LocalDateTime getLastExitedAt(Long userId) {
		return this.exit.getExitTime(userId, user1.getId(), user2.getId());
	}

	public void activate() {
		if (!this.isActive) {
			this.isActive = true;
			touch();
		}
	}

	private void updateActiveIfBothExited() {
		if (exit.bothExited()) {
			this.isActive = false;
		}
	}

	public boolean isArchived() {
		return !this.isActive && this.exit.bothExited(); // 과거 대화 완전 종료된 방
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