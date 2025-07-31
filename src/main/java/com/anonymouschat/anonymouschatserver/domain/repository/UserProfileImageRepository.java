package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.domain.entity.UserProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface UserProfileImageRepository extends JpaRepository<UserProfileImage, Long> {
	List<UserProfileImage> findAllByUserIdAndDeletedIsFalse(Long userId, Sort sort);

	List<UserProfileImage> findAllByUserIdAndDeletedIsFalse(Long userId);
}
