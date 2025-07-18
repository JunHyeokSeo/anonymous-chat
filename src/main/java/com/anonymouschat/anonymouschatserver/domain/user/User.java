package com.anonymouschat.anonymouschatserver.domain.user;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"provider", "provider_id"})
})
public class User {

	@Getter
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
	@Column(name = "nickname", nullable = false, length = 50)
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

	// 활성 여부 (soft delete 대비)
	@Column(name = "active", nullable = false)
	private boolean active = true;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserProfileImage> profileImages = new ArrayList<>();

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

	/**
	 * 테스트용 유저 생성자
	 */
	public User(String nickname) {
		this.provider = OAuthProvider.APPLE;
		this.providerId = "test providerId";
		this.nickname = Objects.requireNonNull(nickname);
		this.gender = Gender.MALE;
		this.age = 29;
		this.region = Region.BUSAN;
		this.bio = "test bio";
	}
}
