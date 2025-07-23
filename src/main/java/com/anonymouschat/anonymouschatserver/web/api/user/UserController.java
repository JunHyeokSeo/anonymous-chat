package com.anonymouschat.anonymouschatserver.web.api.user;

import com.anonymouschat.anonymouschatserver.application.dto.*;
import com.anonymouschat.anonymouschatserver.application.usecase.user.GetMyProfileUseCase;
import com.anonymouschat.anonymouschatserver.application.usecase.user.RegisterUserUseCase;
import com.anonymouschat.anonymouschatserver.application.usecase.user.UpdateUserUseCase;
import com.anonymouschat.anonymouschatserver.application.usecase.user.UserSearchUseCase;
import com.anonymouschat.anonymouschatserver.common.code.SuccessCode;
import com.anonymouschat.anonymouschatserver.common.response.ApiResponse;
import com.anonymouschat.anonymouschatserver.common.security.PrincipalType;
import com.anonymouschat.anonymouschatserver.common.security.TempOAuthPrincipal;
import com.anonymouschat.anonymouschatserver.common.security.UserPrincipal;
import com.anonymouschat.anonymouschatserver.common.security.annotation.ValidPrincipal;
import com.anonymouschat.anonymouschatserver.web.api.user.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final RegisterUserUseCase registerUserUseCase;
	private final GetMyProfileUseCase getMyProfileUseCase;
	private final UpdateUserUseCase updateUserUseCase;
	private final UserSearchUseCase userSearchUseCase;

	/**
	 * 회원 가입
	 * - 임시 토큰 사용 (REGISTRATION)
	 */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<RegisterUserResponse>> registerUser(
			@ValidPrincipal(PrincipalType.REGISTRATION)
			@AuthenticationPrincipal TempOAuthPrincipal principal,
			@RequestPart("request") @Valid RegisterUserRequest request,
			@RequestPart(value = "images", required = false) List<MultipartFile> images
	) throws IOException {
		RegisterUserCommand command = request.toCommand(principal.provider(), principal.providerId());
		List<MultipartFile> safeImages = Optional.ofNullable(images).orElse(List.of());
		Long userId = registerUserUseCase.register(command, safeImages);

		return ResponseEntity.status(HttpStatus.CREATED).body(
				ApiResponse.success(SuccessCode.USER_REGISTERED, new RegisterUserResponse(userId)));
	}

	/**
	 * 내 프로필 조회
	 * - 실제 토큰 사용 (ACCESS)
	 */
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<GetMyProfileResponse>> getMyProfile(
			@ValidPrincipal(PrincipalType.ACCESS)
			@AuthenticationPrincipal UserPrincipal principal
	) {
		GetMyProfileResult result = getMyProfileUseCase.getMyProfile(principal.userId());

		return ResponseEntity.ok(
				ApiResponse.success(SuccessCode.SUCCESS, GetMyProfileResponse.from(result)));
	}

	/**
	 * 내 프로필 수정
	 * - 실제 토큰 사용 (ACCESS)
	 */
	@PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<UpdateUserResponse>> updateUser(
			@ValidPrincipal(PrincipalType.ACCESS)
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestPart("request") @Valid UpdateUserRequest request,
			@RequestPart(value = "images", required = false) List<MultipartFile> images
	) throws IOException {
		UpdateUserCommand command = request.toCommand(principal.userId());
		List<MultipartFile> safeImages = Optional.ofNullable(images).orElse(List.of());
		UpdateUserResult result = updateUserUseCase.update(command, safeImages);

		return ResponseEntity.ok(
				ApiResponse.success(SuccessCode.USER_PROFILE_UPDATED, UpdateUserResponse.from(result)));
	}

	/**
	 * 유저 검색
	 * - 실제 토큰 사용 (ACCESS)
	 */
	@GetMapping("/search")
	public ResponseEntity<ApiResponse<Slice<UserSearchResponse>>> searchUsers(
			@ValidPrincipal(PrincipalType.ACCESS)
			@AuthenticationPrincipal UserPrincipal principal,
			@ModelAttribute UserSearchCondition condition,
			@PageableDefault(size = 30, sort = "lastActiveAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Slice<UserSearchResult> results = userSearchUseCase.search(principal.userId(), condition, pageable);

		Slice<UserSearchResponse> response = results.map(UserSearchResponse::from);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.USER_LIST_FETCHED, response));
	}
}
