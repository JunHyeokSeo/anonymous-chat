package com.anonymouschat.anonymouschatserver.application.service.user;

import com.anonymouschat.anonymouschatserver.application.service.dto.GetMyProfileServiceResult;
import com.anonymouschat.anonymouschatserver.application.service.dto.RegisterUserServiceCommand;
import com.anonymouschat.anonymouschatserver.common.util.ImageValidator;
import com.anonymouschat.anonymouschatserver.domain.user.*;
import com.anonymouschat.anonymouschatserver.infra.file.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserProfileImageRepository userProfileImageRepository;
	private final FileStorage fileStorage;
	private final ImageValidator imageValidator;

	public Long register(RegisterUserServiceCommand request, List<MultipartFile> images) throws IOException {
		User user = request.toEntity();

		if (images != null) {
			for (int i = 0; i < images.size(); i++) {
				MultipartFile image = images.get(i);
				boolean isRepresentative = (i == 0);

				imageValidator.validate(image);
				String imageUrl = fileStorage.upload(image);
				UserProfileImage profileImage = UserProfileImage.builder()
						                                .imageUrl(imageUrl)
						                                .isRepresentative(isRepresentative)
						                                .build();
				user.addProfileImage(profileImage);
			}
		}

		return userRepository.save(user).getId();
	}

	@Transactional(readOnly = true)
	public boolean checkNicknameDuplicate(String nickname) {
		return userRepository.existsByNickname(nickname);
	}

	@Transactional(readOnly = true)
	public GetMyProfileServiceResult getMyProfile(OAuthProvider provider, String providerId){
		User savedUser = userRepository.findByProviderAndProviderId(provider, providerId)
				            .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

		List<UserProfileImage> userProfileImages = userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(savedUser.getId(), Sort.sort(UserProfileImage.class).by(UserProfileImage::getUploadedAt).ascending());

		return GetMyProfileServiceResult.from(savedUser, userProfileImages);
	}

	public void updateUserProfile()
}
