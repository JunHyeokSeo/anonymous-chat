package com.anonymouschat.anonymouschatserver.web.api.controller;

import com.anonymouschat.anonymouschatserver.application.dto.UserUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.UserUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.ApiResponse;
import com.anonymouschat.anonymouschatserver.web.api.dto.UserControllerDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserUseCase userUseCase;

	/**
	 * 회원가입 (Guest만 허용)
	 */
	@PostMapping
	@PreAuthorize("hasRole('Guest')")
	public ResponseEntity<ApiResponse<Long>> register(
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestPart("request") UserControllerDto.RegisterRequest request,
			@RequestPart(value = "images", required = false) List<MultipartFile> images
	) throws IOException {
		Long userId = userUseCase.register(
				UserUseCaseDto.RegisterRequest.from(request, principal.provider(), principal.providerId()),
				images
		);
		return ResponseEntity
				       .status(HttpStatus.CREATED)
				       .body(ApiResponse.success(userId));
	}

	/**
	 * 내 프로필 조회 (User, Admin 허용)
	 */
	@GetMapping("/me")
	@PreAuthorize("hasAnyRole('User','Admin')")
	public ResponseEntity<ApiResponse<UserControllerDto.ProfileResponse>> getMyProfile(
			@AuthenticationPrincipal CustomPrincipal principal
	) {
		UserUseCaseDto.ProfileResponse profile = userUseCase.getMyProfile(principal.userId());
		return ResponseEntity
				       .ok(ApiResponse.success(UserControllerDto.ProfileResponse.from(profile)));
	}

	/**
	 * 내 정보 수정 (User, Admin 허용)
	 */
	@PutMapping("/me")
	@PreAuthorize("hasAnyRole('User','Admin')")
	public ResponseEntity<ApiResponse<Void>> update(
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestPart("request") UserControllerDto.UpdateRequest request,
			@RequestPart(value = "images", required = false) List<MultipartFile> images
	) throws IOException {
		userUseCase.update(UserUseCaseDto.UpdateRequest.from(request, principal.userId()), images);
		return ResponseEntity
				       .ok(ApiResponse.success(null));
	}

	/**
	 * 회원 탈퇴 (User, Admin 허용)
	 */
	@DeleteMapping("/me")
	@PreAuthorize("hasAnyRole('User','Admin')")
	public ResponseEntity<ApiResponse<Void>> withdraw(
			@AuthenticationPrincipal CustomPrincipal principal
	) {
		userUseCase.withdraw(principal.userId());
		return ResponseEntity
				       .ok(ApiResponse.success(null));
	}

	/**
	 * 유저 목록 검색 (User, Admin 허용)
	 */
	@GetMapping
	@PreAuthorize("hasAnyRole('User','Admin')")
	public ResponseEntity<ApiResponse<Slice<UserControllerDto.SearchResponse>>> getUserList(
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid UserControllerDto.SearchConditionRequest request,
			Pageable pageable
	) {
		var searchCondition = UserUseCaseDto.SearchConditionRequest.from(request, principal.userId());
		Slice<UserUseCaseDto.SearchResponse> slice = userUseCase.getUserList(searchCondition, pageable);
		return ResponseEntity
				       .ok(ApiResponse.success(slice.map(UserControllerDto.SearchResponse::from)));
	}
}
