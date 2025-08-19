package com.anonymouschat.anonymouschatserver.web.api.controller;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.CommonResponse;
import com.anonymouschat.anonymouschatserver.web.api.dto.MessageDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

	private final MessageUseCase messageUseCase;

	/**
	 * 메시지 전송 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@PostMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<CommonResponse<MessageDto.SendMessageResponse>> sendMessage(
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestBody MessageDto.SendMessageRequest request
	) {
		Long messageId = messageUseCase.sendMessage(
				MessageDto.SendMessageRequest.from(request, principal.userId())
		);
		return ResponseEntity
				       .status(HttpStatus.CREATED)
				       .body(CommonResponse.success(MessageDto.SendMessageResponse.from(messageId)));
	}

	/**
	 * 채팅방 메시지 조회 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@GetMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<CommonResponse<List<MessageDto.MessageResponse>>> getMessages(
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid MessageDto.GetMessagesRequest request
	) {
		List<MessageUseCaseDto.MessageResponse> responses =
				messageUseCase.getMessages(MessageDto.GetMessagesRequest.from(request, principal.userId()));

		List<MessageDto.MessageResponse> dtoResponses =
				responses.stream().map(MessageDto.MessageResponse::from).toList();

		return ResponseEntity.ok(CommonResponse.success(dtoResponses));
	}

	/**
	 * 메시지 읽음 처리 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@PostMapping("/read")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<CommonResponse<Long>> markMessagesAsRead(
			@AuthenticationPrincipal CustomPrincipal principal,
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
	public ResponseEntity<CommonResponse<Long>> getLastReadMessageIdByOpponent(
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid MessageDto.GetLastReadMessageRequest request
	) {
		Long lastReadMessageId = messageUseCase.getLastReadMessageIdByOpponent(
				MessageDto.GetLastReadMessageRequest.from(request, principal.userId())
		);
		return ResponseEntity.ok(CommonResponse.success(lastReadMessageId));
	}
}
