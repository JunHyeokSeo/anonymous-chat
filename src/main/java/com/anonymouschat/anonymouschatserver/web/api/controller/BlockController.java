package com.anonymouschat.anonymouschatserver.web.api.controller;

import com.anonymouschat.anonymouschatserver.application.usecase.BlockUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.CommonResponse;
import com.anonymouschat.anonymouschatserver.web.api.dto.BlockDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
public class BlockController {

	private final BlockUseCase blockUseCase;

	/**
	 * 유저 차단 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@PostMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<CommonResponse<Void>> blockUser(
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestBody BlockDto.BlockRequest request
	) {
		blockUseCase.blockUser(principal.userId(), request.blockedUserId());
		return ResponseEntity
				       .status(HttpStatus.CREATED)
				       .body(CommonResponse.success(null));
	}

	/**
	 * 유저 차단 해제 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@DeleteMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<CommonResponse<Void>> unblockUser(
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestBody BlockDto.UnblockRequest request
	) {
		blockUseCase.unblockUser(principal.userId(), request.blockedUserId());
		return ResponseEntity
				       .status(HttpStatus.NO_CONTENT)
				       .body(CommonResponse.success(null));
	}
}

