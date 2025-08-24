package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.BadRequestException;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.exception.file.FileUploadException;
import com.anonymouschat.anonymouschatserver.common.exception.user.DuplicateNicknameException;
import com.anonymouschat.anonymouschatserver.common.exception.user.UserNotFoundException;
import com.anonymouschat.anonymouschatserver.common.util.ImageValidator;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.entity.UserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.repository.UserProfileImageRepository;
import com.anonymouschat.anonymouschatserver.domain.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.infra.file.FileStorage;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepository;
	private final UserProfileImageRepository userProfileImageRepository;
	private final FileStorage fileStorage;
	private final ImageValidator imageValidator;

	public User register(UserServiceDto.RegisterCommand command, List<MultipartFile> images) {
		log.info("{}회원 등록 시작 - userId={}", LogTag.USER, command.userId());

		User user = userRepository.findById(command.userId())
				            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_GUEST_NOT_FOUND));

		if (!user.isGuest()) {
			log.warn("{}이미 등록된 유저 접근 차단 - userId={}", LogTag.USER, command.userId());
			throw new BadRequestException(ErrorCode.ALREADY_REGISTERED_USER);
		}

		validateNicknameDuplication(command.nickname());

		user.updateProfile(command.nickname(), command.gender(), command.age(), command.region(), command.bio());
		user.updateRole(Role.USER);

		List<UserProfileImage> profileImages;
		try {
			profileImages = convertToProfileImages(images);
		} catch (IOException e) {
			throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED);
		}
		profileImages.forEach(user::addProfileImage);

		log.info("{}회원 등록 완료 - userId={}", LogTag.USER, user.getId());
		return user;
	}

	public UserServiceDto.ProfileResult getProfile(Long userId) {
		User user = findUser(userId);
		List<UserProfileImage> images = userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(
				user.getId(),
				Sort.sort(UserProfileImage.class).by(UserProfileImage::getUploadedAt).ascending()
		);
		return UserServiceDto.ProfileResult.from(user, images);
	}

	@Transactional(noRollbackFor = FileUploadException.class)
	public void update(UserServiceDto.UpdateCommand command, List<MultipartFile> images) {
		log.info("{}회원 정보 수정 시작 - userId={}", LogTag.USER, command.userId());

		User user = findUser(command.userId());

		//nickName 변경 됐을 때만 중복 검사
		if (!command.nickname().equals(user.getNickname())) {
			validateNicknameDuplication(command.nickname());
		}

		user.updateProfile(command.nickname(), command.gender(), command.age(), command.region(), command.bio());

		try {
			deletePreviousImages(user.getId());
			List<UserProfileImage> newImages = convertToProfileImages(images);
			newImages.forEach(user::addProfileImage);
		} catch (IOException e) {
			log.error("{}프로필 이미지 업로드 실패 - userId={}, 에러={}", LogTag.IMAGE, command.userId(), e.getMessage(), e);
			throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED);
		}

		log.info("{}회원 정보 수정 완료 - userId={}", LogTag.USER, command.userId());
	}


	public void withdraw(Long userId) {
		log.info("{}회원 탈퇴 처리 시작 - userId={}", LogTag.USER, userId);
		findUser(userId).markWithDraw();
		log.info("{}회원 탈퇴 처리 완료 - userId={}", LogTag.USER, userId);
	}

	public Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId) {
		return userRepository.findByProviderAndProviderIdAndActiveTrue(provider, providerId);
	}

	public User findUser(Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
	}

	public User createGuestUser(OAuthProvider provider, String providerId) {
		log.info("{}게스트 유저 생성 요청 - provider={}, providerId={}", LogTag.USER, provider, providerId);

		User guestUser = User.builder()
				                 .provider(provider)
				                 .providerId(providerId)
				                 .role(Role.GUEST)
				                 .nickname("guest_" + System.currentTimeMillis())
				                 .gender(Gender.UNKNOWN)
				                 .age(0)
				                 .region(Region.UNKNOWN)
				                 .bio("")
				                 .build();


		User saved = userRepository.save(guestUser);
		log.info("{}게스트 유저 생성 완료 - userId={}", LogTag.USER, saved.getId());
		return saved;
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
		if (images == null || images.isEmpty()) return new ArrayList<>();

		List<UserProfileImage> result = new ArrayList<>();
		for (int i = 0; i < images.size(); i++) {
			MultipartFile image = images.get(i);
			imageValidator.validate(image);
			try {
				String url = fileStorage.upload(image);
				result.add(UserProfileImage.builder()
						           .imageUrl(url)
						           .isRepresentative(i == 0)
						           .build());
			} catch (IOException e) {
				log.error("{}프로필 이미지 업로드 실패 - index={}, 파일명={}, 에러={}", LogTag.IMAGE, i, image.getOriginalFilename(), e.getMessage(), e);
				throw e;
			}
		}
		return result;
	}
}
