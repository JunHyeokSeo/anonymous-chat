package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.dto.UpdateUserCommand;

public record UpdateUserRequest(

) {
	public UpdateUserCommand toCommand() {
		return null;
	}
}
