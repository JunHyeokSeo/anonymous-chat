package com.anonymouschat.anonymouschatserver.domain.message.entity;

import com.anonymouschat.anonymouschatserver.domain.chatroom.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@AllArgsConstructor
@Table(name = "message")
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "is_read", nullable = false)
	private boolean isRead = false;

	@Column(name = "sent_at", nullable = false)
	private LocalDateTime sentAt;

	@PrePersist
	private void initSentAt() {
		this.sentAt = LocalDateTime.now();
	}

	public boolean isSentBy(Long userId) {
		return sender.getId().equals(userId);
	}
}
