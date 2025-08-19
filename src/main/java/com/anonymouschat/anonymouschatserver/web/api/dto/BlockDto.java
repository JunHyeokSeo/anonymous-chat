package com.anonymouschat.anonymouschatserver.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class BlockDto {

	public record BlockRequest(
			@Schema(description = "차단할 대상 사용자 ID", example = "12345")
			@NotNull(message = "차단할 대상 사용자 ID는 필수입니다.")
			Long blockedUserId
	) {}

	public record UnblockRequest(
			@Schema(description = "차단 해제할 대상 사용자 ID", example = "12345")
			@NotNull(message = "차단 해제할 대상 사용자 ID는 필수입니다.")
			Long blockedUserId
	) {}
}