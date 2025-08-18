package com.anonymouschat.anonymouschatserver.web.api.dto;

import jakarta.validation.constraints.NotNull;

public class BlockDto {
	public record BlockRequest(
			@NotNull(message = "차단할 대상 사용자 ID는 필수입니다.")
			Long blockedUserId
	) {}

	public record UnblockRequest(
			@NotNull(message = "차단 해제할 대상 사용자 ID는 필수입니다.")
			Long blockedUserId
	) {}
}