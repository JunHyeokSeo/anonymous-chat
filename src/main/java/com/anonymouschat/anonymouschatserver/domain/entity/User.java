package com.anonymouschat.anonymouschatserver.domain.entity;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.BadRequestException;
import com.anonymouschat.anonymouschatserver.domain.type.*;
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

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private Role role;

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
			throw new BadRequestException(ErrorCode.PROFILE_IMAGE_LIMIT_EXCEEDED);
		}

		profileImages.add(image);
		image.setUser(this);
	}

	public void updateProfile(String nickname, Gender gender, int age, Region region, String bio) {
		this.nickname = Objects.requireNonNull(nickname);
		this.gender = Objects.requireNonNull(gender);
		this.age = age;
		this.region = Objects.requireNonNull(region);
		this.bio = bio == null ? "" : bio;
	}

	public void markWithDraw() {
		this.active = false;
	}

	public boolean isGuest() {
		return Role.GUEST.equals(this.role);
	}

	public void updateRole(Role newRole) {
		if (newRole == null) {
			throw new IllegalArgumentException("역할은 null이 될 수 없습니다.");
		}

		this.role = newRole;
	}

	@Builder
	public User(OAuthProvider provider, String providerId, Role role, String nickname, Gender gender, int age, Region region, String bio) {
		this.provider = Objects.requireNonNull(provider);
		this.providerId = Objects.requireNonNull(providerId);
		this.role = Objects.requireNonNull(role);
		this.nickname = nickname;
		this.gender = gender;
		this.age = age;
		this.region = region;
		this.bio = bio == null ? "" : bio;
	}
}