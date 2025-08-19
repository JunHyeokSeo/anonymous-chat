package com.anonymouschat.anonymouschatserver.web.api.controller;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.ChatRoomUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.CommonResponse;
import com.anonymouschat.anonymouschatserver.web.api.dto.ChatRoomDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomUseCase chatRoomUseCase;

	/**
	 * 채팅방 생성 또는 기존 채팅방 조회 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@PostMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<CommonResponse<ChatRoomDto.CreateResponse>> createOrFind(
			@AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestBody ChatRoomDto.CreateRequest request
	) {
		Long roomId = chatRoomUseCase.createOrFind(principal.userId(), request.recipientId());
		return ResponseEntity
				       .status(HttpStatus.CREATED)
				       .body(CommonResponse.success(ChatRoomDto.CreateResponse.from(roomId)));
	}

	/**
	 * 내가 참여 중인 채팅방 목록 조회 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@GetMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<CommonResponse<List<ChatRoomDto.SummaryResponse>>> getMyActiveChatRooms(
			@AuthenticationPrincipal CustomPrincipal principal
	) {
		List<ChatRoomUseCaseDto.SummaryResponse> result = chatRoomUseCase.getMyActiveChatRooms(principal.userId());
		List<ChatRoomDto.SummaryResponse> responses = result.stream()
				                                              .map(ChatRoomDto.SummaryResponse::from)
				                                              .toList();
		return ResponseEntity
				       .ok(CommonResponse.success(responses));
	}

	/**
	 * 채팅방 나가기 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@DeleteMapping("/{roomId}")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<CommonResponse<Void>> exitChatRoom(
			@AuthenticationPrincipal CustomPrincipal principal,
			@PathVariable Long roomId
	) {
		chatRoomUseCase.exitChatRoom(principal.userId(), roomId);
		return ResponseEntity
				       .status(HttpStatus.NO_CONTENT)
				       .body(CommonResponse.success(null));
	}
}
