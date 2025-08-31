package com.anonymouschat.anonymouschatserver.presentation.controller;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.dto.UserUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.application.usecase.UserUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.presentation.CommonResponse;
import com.anonymouschat.anonymouschatserver.presentation.controller.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

	private final UserUseCase userUseCase;
	private final AuthUseCase authUseCase;

	@PostMapping
	@PreAuthorize("hasRole('GUEST')")
	@Operation(summary = "회원가입", description = "신규 사용자가 회원가입합니다. (Guest만 허용)")
	@ApiResponse(ref = "Created")
	@ApiResponse(ref = "BadRequest")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<UserDto.RegisterResponse>> register(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestPart("request") UserDto.RegisterRequest request,
			@RequestPart(value = "images", required = false) List<MultipartFile> images
	) throws IOException {

		UserUseCaseDto.RegisterResponse registeredUserInfo =
				userUseCase.register(UserUseCaseDto.RegisterRequest.from(request, principal.userId()), images);

		AuthUseCaseDto.AuthData authResult =
				authUseCase.login(registeredUserInfo.provider(), registeredUserInfo.providerId());

		return ResponseEntity
				       .status(HttpStatus.CREATED)
				       .body(CommonResponse.success(
						       UserDto.RegisterResponse.builder()
								       .accessToken(authResult.accessToken())
								       .build()));
	}

	@GetMapping("/me")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<UserDto.ProfileResponse>> getMyProfile(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal
	) {
		UserUseCaseDto.ProfileResponse profile = userUseCase.getProfile(principal.userId());
		return ResponseEntity.ok(CommonResponse.success(UserDto.ProfileResponse.from(profile)));
	}

	@PutMapping("/me")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "내 정보 수정", description = "내 정보를 수정합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "BadRequest")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<Void>> update(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestPart("request") UserDto.UpdateRequest request,
			@RequestPart(value = "images", required = false) List<MultipartFile> images
	) {
		userUseCase.update(UserUseCaseDto.UpdateRequest.from(request, principal.userId()), images);
		return ResponseEntity.ok(CommonResponse.success(null));
	}

	@DeleteMapping("/me")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "회원 탈퇴", description = "회원 탈퇴 처리합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<Void>> withdraw(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal
	) {
		userUseCase.withdraw(principal.userId());
		return ResponseEntity.ok(CommonResponse.success(null));
	}

	@GetMapping("/{userId}")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "사용자 프로필 조회", description = "특정 사용자의 프로필을 조회합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<UserDto.ProfileResponse>> getUserProfile(
			@PathVariable Long userId
	) {
		UserUseCaseDto.ProfileResponse profile = userUseCase.getProfile(userId);
		return ResponseEntity.ok(CommonResponse.success(UserDto.ProfileResponse.from(profile)));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "유저 목록 검색", description = "검색 조건에 맞는 유저 목록을 Slice 페이징으로 조회합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<Slice<UserDto.SearchResponse>>> getUserList(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			@Valid UserDto.SearchConditionRequest request,
			Pageable pageable
	) {
		var searchCondition = UserUseCaseDto.SearchConditionRequest.from(request, principal.userId());
		Slice<UserUseCaseDto.SearchResponse> slice = userUseCase.getUserList(searchCondition, pageable);
		return ResponseEntity.ok(CommonResponse.success(slice.map(UserDto.SearchResponse::from)));
	}
}
