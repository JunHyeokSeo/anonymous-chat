package com.anonymouschat.anonymouschatserver.domain.block.entity;

import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Table(name = "block")
public class Block {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Getter
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "blocker_id", nullable = false)
	private User blocker;

	@Getter
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "blocked_id", nullable = false)
	private User blocked;

	@Getter
	@Column(name = "active", nullable = false)
	private boolean active;

	@Getter
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Builder
	public Block(User blocker, User blocked) {
		this.blocker = blocker;
		this.blocked = blocked;
		this.active = true;
		this.createdAt = LocalDateTime.now();
	}

	public void deactivate() {
		this.active = false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Block other)) return false;
		return Objects.equals(blocker, other.blocker) && Objects.equals(blocked, other.blocked);
	}

	@Override
	public int hashCode() {
		return Objects.hash(blocker, blocked);
	}

}

