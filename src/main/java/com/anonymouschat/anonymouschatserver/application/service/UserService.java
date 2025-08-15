package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.*;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.user.DuplicateNicknameException;
import com.anonymouschat.anonymouschatserver.common.exception.user.UserNotFoundException;
import com.anonymouschat.anonymouschatserver.common.util.ImageValidator;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.entity.UserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.repository.UserProfileImageRepository;
import com.anonymouschat.anonymouschatserver.domain.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.file.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserProfileImageRepository userProfileImageRepository;
	private final FileStorage fileStorage;
	private final ImageValidator imageValidator;

	public Long register(UserServiceDto.RegisterCommand command, List<MultipartFile> images) throws IOException {
		validateNicknameDuplication(command.nickname());
		User user = command.toEntity();
		List<UserProfileImage> profileImages = convertToProfileImages(images);
		profileImages.forEach(user::addProfileImage);
		return userRepository.save(user).getId();
	}

	@Transactional(readOnly = true)
	public UserServiceDto.ProfileResult getMyProfile(Long userId) {
		User user = findUser(userId);
		List<UserProfileImage> images = userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(
				user.getId(),
				Sort.sort(UserProfileImage.class).by(UserProfileImage::getUploadedAt).ascending()
		);
		return UserServiceDto.ProfileResult.from(user, images);
	}

	public void update(UserServiceDto.UpdateCommand command, List<MultipartFile> images) throws IOException {
		User user = findUser(command.userId());

		//닉네임 중복 검사
		validateNicknameDuplication(command.nickname());

		user.update(command);

		//기존 프로필 이미지 삭제
		deletePreviousImages(user.getId());

		//새 프로필 이미지 등록
		List<UserProfileImage> newImages = convertToProfileImages(images);
		newImages.forEach(user::addProfileImage);
	}

	public void withdraw(Long userId) {
		findUser(userId).markWithDraw();
	}

	@Transactional(readOnly = true)
	public Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId) {
		return userRepository.findByProviderAndProviderIdAndActiveTrue(provider, providerId);
	}

	@Transactional(readOnly = true)
	public User findUser(Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
	}

	private void validateNicknameDuplication(String nickname) {
		if (userRepository.existsByNickname(nickname)) {
			throw new DuplicateNicknameException(ErrorCode.DUPLICATE_NICKNAME);
		}
	}

	private void deletePreviousImages(Long userId) {
		List<UserProfileImage> images = userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(userId);
		for (UserProfileImage image : images) {
			fileStorage.delete(image.getImageUrl());
			image.softDelete();
		}
	}

	private List<UserProfileImage> convertToProfileImages(List<MultipartFile> images) throws IOException {
		List<UserProfileImage> result = new ArrayList<>();
		for (int i = 0; i < images.size(); i++) {
			MultipartFile image = images.get(i);
			imageValidator.validate(image);
			String url = fileStorage.upload(image);
			result.add(UserProfileImage.builder()
					           .imageUrl(url)
					           .isRepresentative(i == 0)
					           .build());
		}
		return result;
	}
}