package com.anonymouschat.anonymouschatserver.web.api.user;

import com.anonymouschat.anonymouschatserver.application.usecase.user.GetMyProfileUseCase;
import com.anonymouschat.anonymouschatserver.application.usecase.user.RegisterUserUseCase;
import com.anonymouschat.anonymouschatserver.application.dto.GetMyProfileResult;
import com.anonymouschat.anonymouschatserver.application.dto.RegisterUserCommand;
import com.anonymouschat.anonymouschatserver.common.code.SuccessCode;
import com.anonymouschat.anonymouschatserver.common.jwt.OAuthPrincipal;
import com.anonymouschat.anonymouschatserver.common.response.ApiResponse;
import com.anonymouschat.anonymouschatserver.web.api.user.dto.GetMyProfileResponse;
import com.anonymouschat.anonymouschatserver.web.api.user.dto.RegisterUserRequest;
import com.anonymouschat.anonymouschatserver.web.api.user.dto.RegisterUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final RegisterUserUseCase registerUserUseCase;
	private final GetMyProfileUseCase getMyProfileUseCase;

	@PostMapping(value = "/api/v1/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<RegisterUserResponse>> registerUser(
			@AuthenticationPrincipal OAuthPrincipal oAuthPrincipal,
			@RequestPart("request") @Valid RegisterUserRequest request,
			@RequestPart(value = "images", required = false) List<MultipartFile> images
	) throws IOException {
		RegisterUserCommand command = request.toCommand(oAuthPrincipal.provider(), oAuthPrincipal.providerId());
		Long userId = registerUserUseCase.register(command, images);

		return ResponseEntity.status(HttpStatus.CREATED).body(
				ApiResponse.success(SuccessCode.USER_REGISTERED, new RegisterUserResponse(userId)));
	}

	@GetMapping(value = "/api/v1/users/me")
	public ResponseEntity<ApiResponse<GetMyProfileResponse>> getMyProfile(@AuthenticationPrincipal OAuthPrincipal oAuthPrincipal) {
		GetMyProfileResult myProfile = getMyProfileUseCase.getMyProfile(oAuthPrincipal.provider(), oAuthPrincipal.providerId());

		return ResponseEntity.status(HttpStatus.OK).body(
				ApiResponse.success(SuccessCode.SUCCESS, GetMyProfileResponse.from(myProfile)));
	}
}
