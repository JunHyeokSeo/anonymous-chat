package com.anonymouschat.anonymouschatserver.web.controller;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.CommonResponse;
import com.anonymouschat.anonymouschatserver.web.controller.dto.MessageDto;
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
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Message", description = "메시지 관련 API")
public class MessageController {

	private final MessageUseCase messageUseCase;

	/**
	 * 메시지 전송 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@PostMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(
			summary = "메시지 전송",
			description = "특정 채팅방에 메시지를 전송합니다. (USER, ADMIN 권한 필요)",
			responses = {
					@ApiResponse(
							responseCode = "201",
							description = "메시지 전송 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "메시지 전송 성공 예시",
											value = """
                        {
                          "success": true,
                          "data": {
                            "messageId": 2001
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
	public ResponseEntity<CommonResponse<MessageDto.SendMessageResponse>> sendMessage(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
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
	@Operation(
			summary = "채팅방 메시지 조회",
			description = "특정 채팅방의 메시지를 페이징 조회합니다. (USER, ADMIN 권한 필요)",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "조회 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "메시지 조회 성공 예시",
											value = """
                        {
                          "success": true,
                          "data": [
                            {
                              "messageId": 2001,
                              "roomId": 1001,
                              "senderId": 3001,
                              "content": "안녕하세요! 반갑습니다 :)",
                              "sentAt": "2025-08-19T13:24:00",
                              "isMine": true
                            },
                            {
                              "messageId": 2002,
                              "roomId": 1001,
                              "senderId": 3002,
                              "content": "네, 반갑습니다!",
                              "sentAt": "2025-08-19T13:25:00",
                              "isMine": false
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
	public ResponseEntity<CommonResponse<List<MessageDto.MessageResponse>>> getMessages(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
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
	@Operation(
			summary = "메시지 읽음 처리",
			description = "특정 채팅방의 메시지들을 읽음 처리합니다. (USER, ADMIN 권한 필요)",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "읽음 처리된 메시지 개수 반환",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "읽음 처리 성공 예시",
											value = """
                        {
                          "success": true,
                          "data": 5,
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
	@Operation(
			summary = "상대방의 마지막 읽은 메시지 ID 조회",
			description = "특정 채팅방에서 상대방이 마지막으로 읽은 메시지 ID를 조회합니다. (USER, ADMIN 권한 필요)",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "상대방의 마지막 읽은 메시지 ID 반환",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "마지막 읽은 메시지 ID 조회 성공 예시",
											value = """
                        {
                          "success": true,
                          "data": 2005,
                          "error": null
                        }
                        """
									)
							)
					),
					@ApiResponse(responseCode = "401", description = "인증 실패"),
					@ApiResponse(responseCode = "404", description = "채팅방 또는 메시지 없음")
			}
	)
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

