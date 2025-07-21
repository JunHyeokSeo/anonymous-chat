package com.anonymouschat.anonymouschatserver.domain.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "user_profile_image")
public class UserProfileImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	@Column(name = "is_representative", nullable = false)
	private boolean isRepresentative;

	@Column(name = "uploaded_at", nullable = false)
	private LocalDateTime uploadedAt = LocalDateTime.now();

	@Column(name = "deleted", nullable = false)
	private boolean deleted = false;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public void softDelete() {
		this.deleted = true;
	}

	protected void setUser(User user) {
		this.user = user;
	}

	@Builder
	public UserProfileImage(String imageUrl, boolean isRepresentative) {
		this.imageUrl = imageUrl;
		this.isRepresentative = isRepresentative;
	}
}
