package com.anonymouschat.anonymouschatserver.application.service.user;

import com.anonymouschat.anonymouschatserver.application.dto.*;
import com.anonymouschat.anonymouschatserver.common.util.ImageValidator;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import com.anonymouschat.anonymouschatserver.domain.user.entity.UserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.user.repository.UserProfileImageRepository;
import com.anonymouschat.anonymouschatserver.domain.user.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.file.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserProfileImageRepository userProfileImageRepository;
	private final FileStorage fileStorage;
	private final ImageValidator imageValidator;

	public Long register(RegisterUserCommand command, List<MultipartFile> images) throws IOException {
		User user = command.toEntity();

		List<UserProfileImage> profileImages = convertToProfileImages(images);
		profileImages.forEach(user::addProfileImage);

		return userRepository.save(user).getId();
	}

	@Transactional(readOnly = true)
	public boolean checkNicknameDuplicate(String nickname) {
		return userRepository.existsByNickname(nickname);
	}

	@Transactional(readOnly = true)
	public GetMyProfileResult getMyProfile(OAuthProvider provider, String providerId) {
		User user = userRepository.findByProviderAndProviderId(provider, providerId)
				            .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

		List<UserProfileImage> profileImages =
				userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(
						user.getId(),
						Sort.sort(UserProfileImage.class).by(UserProfileImage::getUploadedAt).ascending()
				);

		return GetMyProfileResult.from(user, profileImages);
	}

	public UpdateUserResult update(UpdateUserCommand command, List<MultipartFile> images) throws IOException {
		User user = userRepository.findById(command.id())
				            .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

		user.update(command);

		deletePrevProfileImages(user.getId());

		List<UserProfileImage> newImages = convertToProfileImages(images);
		newImages.forEach(user::addProfileImage);

		return UpdateUserResult.from(user, newImages);
	}

	private void deletePrevProfileImages(Long userId) {
		List<UserProfileImage> prevImages = userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(userId);

		for (UserProfileImage image : prevImages) {
			fileStorage.delete(image.getImageUrl()); // 실제 파일 삭제
			image.softDelete(); // 논리적 삭제 (deleted = true)
		}
	}

	private List<UserProfileImage> convertToProfileImages(List<MultipartFile> images) throws IOException {
		List<UserProfileImage> result = new ArrayList<>();

		for (int i = 0; i < images.size(); i++) {
			MultipartFile image = images.get(i);
			boolean isRepresentative = (i == 0);

			imageValidator.validate(image);
			String imageUrl = fileStorage.upload(image);
			result.add(toUserProfileImage(imageUrl, isRepresentative));
		}

		return result;
	}

	private UserProfileImage toUserProfileImage(String imageUrl, boolean isRepresentative) {
		return UserProfileImage.builder()
				       .imageUrl(imageUrl)
				       .isRepresentative(isRepresentative)
				       .build();
	}
}
