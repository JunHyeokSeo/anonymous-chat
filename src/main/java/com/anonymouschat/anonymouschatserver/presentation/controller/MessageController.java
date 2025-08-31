package com.anonymouschat.anonymouschatserver.presentation.controller;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.presentation.CommonResponse;
import com.anonymouschat.anonymouschatserver.presentation.controller.dto.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Message", description = "메시지 관련 API")
public class MessageController {

	private final MessageUseCase messageUseCase;

	/**
	 * 채팅방 메시지 조회 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@GetMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "채팅방 메시지 조회", description = "특정 채팅방의 메시지를 페이징 조회합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<List<MessageDto.MessageResponse>>> getMessages(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			@Valid MessageDto.GetMessagesRequest request
	) {
		List<MessageUseCaseDto.MessageResponse> responses =
				messageUseCase.getMessages(MessageDto.GetMessagesRequest.from(request, principal.userId()));

		List<MessageDto.MessageResponse> dtoResponses = responses.stream()
				                                                .map(MessageDto.MessageResponse::from)
				                                                .toList();

		return ResponseEntity.ok(CommonResponse.success(dtoResponses));
	}

	/**
	 * 메시지 읽음 처리 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@PostMapping("/read")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "메시지 읽음 처리", description = "특정 채팅방의 메시지들을 읽음 처리합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "BadRequest")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<Long>> markMessagesAsRead(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestBody MessageDto.MarkMessagesAsReadRequest request
	) {
		Long updatedCount = messageUseCase.markMessagesAsRead(
				MessageDto.MarkMessagesAsReadRequest.from(request, principal.userId())
		);
		return ResponseEntity.ok(CommonResponse.success(updatedCount));
	}

	/**
	 * 상대방의 마지막 읽은 메시지 ID 조회 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@GetMapping("/last-read")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "상대방의 마지막 읽은 메시지 ID 조회", description = "특정 채팅방에서 상대방이 마지막으로 읽은 메시지 ID를 조회합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "Unauthorized")
	@ApiResponse(responseCode = "404", description = "채팅방 또는 메시지 없음")
	public ResponseEntity<CommonResponse<Long>> getLastReadMessageIdByOpponent(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			@Valid MessageDto.GetLastReadMessageRequest request
	) {
		Long lastReadMessageId = messageUseCase.getLastReadMessageIdByOpponent(
				MessageDto.GetLastReadMessageRequest.from(request, principal.userId())
		);
		return ResponseEntity.ok(CommonResponse.success(lastReadMessageId));
	}
}
