package com.anonymouschat.anonymouschatserver.domain.user.entity;

import com.anonymouschat.anonymouschatserver.application.dto.UpdateUserCommand;
import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;
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
@Table(name = "user", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"provider", "provider_id"})
})
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	// OAuth 정보
	@Column(name = "provider", nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private OAuthProvider provider; // GOOGLE, APPLE 등

	@Column(name = "provider_id", nullable = false, length = 100)
	private String providerId; // ex: sub 값 or user id from provider

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

	@Column(name = "bio", length = 255)
	private String bio;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	@Column(name = "last_active_at", nullable = false)
	private LocalDateTime lastActiveAt = LocalDateTime.now();

	// 활성 여부 (soft delete 대비)
	@Column(name = "active", nullable = false)
	private boolean active = true;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserProfileImage> profileImages = new ArrayList<>();

	public void addProfileImage(UserProfileImage image) {
		profileImages.add(image);
		image.setUser(this);
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public void update(UpdateUserCommand command) {
		updateProfile(command.nickname(), command.gender(), command.age(), command.region(), command.bio());
	}

	private void updateProfile(String nickname, Gender gender, int age, Region region, String bio) {
		this.nickname = Objects.requireNonNull(nickname);
		this.gender = Objects.requireNonNull(gender);
		this.age = age;
		this.region = Objects.requireNonNull(region);
		this.bio = bio == null ? "" : bio;
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
