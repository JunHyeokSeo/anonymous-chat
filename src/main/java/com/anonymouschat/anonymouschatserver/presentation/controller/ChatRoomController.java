package com.anonymouschat.anonymouschatserver.presentation.controller;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.ChatRoomUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.presentation.CommonResponse;
import com.anonymouschat.anonymouschatserver.presentation.controller.dto.ChatRoomDto;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 관련 API")
public class ChatRoomController {

	private final ChatRoomUseCase chatRoomUseCase;

	/**
	 * 채팅방 생성 또는 기존 채팅방 조회 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@PostMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(
			summary = "채팅방 생성 또는 조회",
			description = "상대방과의 채팅방을 새로 생성하거나, 이미 존재하면 해당 채팅방을 반환합니다. (USER, ADMIN 권한 필요)",
			responses = {
					@ApiResponse(
							responseCode = "201",
							description = "채팅방 생성 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "채팅방 생성 성공 예시",
											value = """
                        {
                          "success": true,
                          "data": {
                            "roomId": 1001
                          },
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
	public ResponseEntity<CommonResponse<ChatRoomDto.CreateResponse>> createOrFind(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
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
	@Operation(
			summary = "참여 중인 채팅방 목록 조회",
			description = "현재 로그인한 사용자가 참여 중인 모든 채팅방을 조회합니다. (USER, ADMIN 권한 필요)",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "조회 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "채팅방 목록 조회 예시",
											value = """
                        {
                          "success": true,
                          "data": [
                            {
                              "roomId": 1001,
                              "opponentId": 42,
                              "opponentNickname": "밍글유저",
                              "opponentAge": 25,
                              "opponentRegion": "서울",
                              "opponentProfileImageUrl": "https://example.com/profile.png",
                              "lastMessageTime": "2025-08-19T15:30:00"
                            }
                          ],
                          "error": null
                        }
                        """
									)
							)
					),
					@ApiResponse(responseCode = "401", description = "인증 실패")
			}
	)
	public ResponseEntity<CommonResponse<List<ChatRoomDto.SummaryResponse>>> getMyActiveChatRooms(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal
	) {
		List<ChatRoomUseCaseDto.SummaryResponse> result = chatRoomUseCase.getMyActiveChatRooms(principal.userId());
		List<ChatRoomDto.SummaryResponse> responses = result.stream()
				                                              .map(ChatRoomDto.SummaryResponse::from)
				                                              .toList();
		return ResponseEntity.ok(CommonResponse.success(responses));
	}

	/**
	 * 채팅방 나가기 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@DeleteMapping("/{roomId}")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(
			summary = "채팅방 나가기",
			description = "현재 로그인한 사용자가 특정 채팅방을 나갑니다. (USER, ADMIN 권한 필요)",
			responses = {
					@ApiResponse(
							responseCode = "204",
							description = "채팅방 나가기 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "채팅방 나가기 성공 예시",
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
					@ApiResponse(responseCode = "401", description = "인증 실패"),
					@ApiResponse(responseCode = "404", description = "해당 채팅방 없음")
			}
	)
	public ResponseEntity<CommonResponse<Void>> exitChatRoom(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			@Parameter(description = "채팅방 ID", example = "1001")
			@PathVariable Long roomId
	) {
		chatRoomUseCase.exitChatRoom(principal.userId(), roomId);
		return ResponseEntity
				       .status(HttpStatus.NO_CONTENT)
				       .body(CommonResponse.success(null));
	}
}
