package com.anonymouschat.anonymouschatserver.presentation.controller;

import com.anonymouschat.anonymouschatserver.application.usecase.ChatRoomUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.presentation.CommonResponse;
import com.anonymouschat.anonymouschatserver.presentation.controller.dto.ChatRoomDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 관련 API")
public class ChatRoomController {

	private final ChatRoomUseCase chatRoomUseCase;

	@PostMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "채팅방 생성 또는 조회", description = "상대방과의 채팅방을 생성하거나 이미 존재하면 반환합니다.")
	@ApiResponse(ref = "Created")
	@ApiResponse(ref = "BadRequest")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<ChatRoomDto.CreateResponse>> createOrFind(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			@Valid @RequestBody ChatRoomDto.CreateRequest request
	) {
		Long roomId = chatRoomUseCase.createOrFind(principal.userId(), request.recipientId());
		return ResponseEntity
				       .status(HttpStatus.CREATED)
				       .body(CommonResponse.success(ChatRoomDto.CreateResponse.from(roomId)));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "참여 중인 채팅방 목록 조회", description = "현재 로그인한 사용자가 참여 중인 모든 채팅방을 조회합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<List<ChatRoomDto.SummaryResponse>>> getMyActiveChatRooms(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal
	) {
		var result = chatRoomUseCase.getMyActiveChatRooms(principal.userId());
		var responses = result.stream().map(ChatRoomDto.SummaryResponse::from).toList();
		return ResponseEntity.ok(CommonResponse.success(responses));
	}

	@DeleteMapping("/{roomId}")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "채팅방 나가기", description = "현재 로그인한 사용자가 특정 채팅방을 나갑니다.")
	@ApiResponse(responseCode = "204", description = "나가기 성공")
	@ApiResponse(ref = "Unauthorized")
	@ApiResponse(responseCode = "404", description = "채팅방 없음")
	public ResponseEntity<CommonResponse<Void>> exitChatRoom(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			@PathVariable Long roomId
	) {
		chatRoomUseCase.exitChatRoom(principal.userId(), roomId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success(null));
	}

	@GetMapping("/{roomId}")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "채팅방 단건 조회", description = "roomId로 특정 채팅방을 조회합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "Unauthorized")
	@ApiResponse(responseCode = "404", description = "채팅방 없음")
	public ResponseEntity<CommonResponse<ChatRoomDto.SummaryResponse>> getChatRoom(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			@PathVariable Long roomId
	) {
		var response = chatRoomUseCase.getChatRoom(principal.userId(), roomId);
		return ResponseEntity.ok(CommonResponse.success(ChatRoomDto.SummaryResponse.from(response)));
	}
}

