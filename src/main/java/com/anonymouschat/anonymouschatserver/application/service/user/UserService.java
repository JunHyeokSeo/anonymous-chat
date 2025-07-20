package com.anonymouschat.anonymouschatserver.application.service.user;

import com.anonymouschat.anonymouschatserver.application.service.dto.RegisterUserServiceRequest;
import com.anonymouschat.anonymouschatserver.common.util.ImageValidator;
import com.anonymouschat.anonymouschatserver.domain.user.User;
import com.anonymouschat.anonymouschatserver.domain.user.UserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.user.UserRepository;
import com.anonymouschat.anonymouschatserver.infra.file.FileStorage;
import lombok.RequiredArgsConstructor;
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
	private final FileStorage fileStorage;
	private final ImageValidator imageValidator;


	public Long register(RegisterUserServiceRequest request, List<MultipartFile> images) throws IOException {
		User user = User.builder()
				            .nickname(request.nickname())
				            .gender(request.gender())
				            .age(request.age())
				            .region(request.region())
				            .bio(request.bio())
				            .provider(request.provider())
				            .providerId(request.providerId())
				            .build();

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
				user.getProfileImages().add(profileImage);
			}
		}

		return userRepository.save(user).getId();
	}

	@Transactional(readOnly = true)
	public boolean checkNicknameDuplicate(String nickname) {
		return userRepository.existsByNickname(nickname);
	}
}
