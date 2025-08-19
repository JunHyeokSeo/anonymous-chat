package com.anonymouschat.anonymouschatserver.web.controller;

import com.anonymouschat.anonymouschatserver.application.usecase.BlockUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.CommonResponse;
import com.anonymouschat.anonymouschatserver.web.controller.dto.BlockDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Block", description = "유저 차단 관련 API")
public class BlockController {

	private final BlockUseCase blockUseCase;

	@PostMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(
			summary = "유저 차단",
			description = "현재 로그인된 사용자가 다른 사용자를 차단합니다. (USER, ADMIN 권한 필요)",
			responses = {
					@ApiResponse(
							responseCode = "201",
							description = "차단 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "차단 성공 예시",
											value = """
                        {
                          "success": true,
                          "data": null,
                          "error": null
                        }
                        """
									)
							)
					),
					@ApiResponse(responseCode = "400", description = "잘못된 요청"),
					@ApiResponse(responseCode = "401", description = "인증 실패")
			}
	)
	public ResponseEntity<CommonResponse<Void>> blockUser(
			@Parameter(hidden = true)
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestBody BlockDto.BlockRequest request
	) {
		blockUseCase.blockUser(principal.userId(), request.blockedUserId());
		return ResponseEntity
				       .status(HttpStatus.CREATED)
				       .body(CommonResponse.success(null));
	}

	@DeleteMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(
			summary = "유저 차단 해제",
			description = "현재 로그인된 사용자가 이전에 차단한 사용자를 차단 해제합니다. (USER, ADMIN 권한 필요)",
			responses = {
					@ApiResponse(
							responseCode = "204",
							description = "차단 해제 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "차단 해제 성공 예시",
											value = """
                        {
                          "success": true,
                          "data": null,
                          "error": null
                        }
                        """
									)
							)
					),
					@ApiResponse(responseCode = "400", description = "잘못된 요청"),
					@ApiResponse(responseCode = "401", description = "인증 실패"),
					@ApiResponse(responseCode = "404", description = "차단 정보 없음")
			}
	)
	public ResponseEntity<CommonResponse<Void>> unblockUser(
			@Parameter(hidden = true)
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestBody BlockDto.UnblockRequest request
	) {
		blockUseCase.unblockUser(principal.userId(), request.blockedUserId());
		return ResponseEntity
				       .status(HttpStatus.NO_CONTENT)
				       .body(CommonResponse.success(null));
	}
}