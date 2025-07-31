package com.anonymouschat.anonymouschatserver.domain.entity;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "`user`", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"provider", "provider_id"})
})
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	/**
	 * 인증 공급자 (GOOGLE, KAKAO 등)
	 */
	@Column(name = "provider", nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private OAuthProvider provider; // GOOGLE, APPLE 등

	/**
	 * OAuth 공급자(provider)가 보장하는 고유 식별자 (ex. JWT의 sub 값)
	 */
	@Column(name = "provider_id", nullable = false, length = 100)
	private String providerId;

	// 사용자 정보
	@Column(name = "nickname", nullable = false, length = 50, unique = true)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender", nullable = false, length = 10)
	private Gender gender;

	@Column(name = "age", nullable = false)
	private int age;

	@Enumerated(EnumType.STRING)
	@Column(name = "region", nullable = false, length = 50)
	private Region region;

	@Column(name = "bio")
	private String bio;

	@Column(name = "created_at", nullable = false)
	private final LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	@Column(name = "last_active_at", nullable = false)
	private final LocalDateTime lastActiveAt = LocalDateTime.now();

	// 활성 여부 (soft delete 대비)
	@Column(name = "active", nullable = false)
	private boolean active = true;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<UserProfileImage> profileImages = new ArrayList<>();

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public void addProfileImage(UserProfileImage image) {
		if (profileImages.size() >= 3) {
			throw new IllegalStateException("프로필 이미지는 최대 3장까지만 등록할 수 있습니다.");
		}

		profileImages.add(image);
		image.setUser(this);
	}

	public void update(UserServiceDto.UpdateCommand command) {
		updateProfile(command.nickname(), command.gender(), command.age(), command.region(), command.bio());
	}

	private void updateProfile(String nickname, Gender gender, int age, Region region, String bio) {
		this.nickname = Objects.requireNonNull(nickname);
		this.gender = Objects.requireNonNull(gender);
		this.age = age;
		this.region = Objects.requireNonNull(region);
		this.bio = bio == null ? "" : bio;
	}

	public void markWithDraw() {
		this.active = false;
	}

	@Builder
	public User(OAuthProvider provider, String providerId, String nickname, Gender gender, int age, Region region, String bio) {
		this.provider = Objects.requireNonNull(provider);
		this.providerId = Objects.requireNonNull(providerId);
		this.nickname = Objects.requireNonNull(nickname);
		this.gender = Objects.requireNonNull(gender);
		this.age = age;
		this.region = Objects.requireNonNull(region);
		this.bio = bio == null ? "" : bio;
	}
}
