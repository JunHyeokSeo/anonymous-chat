package com.anonymouschat.anonymouschatserver.common.exception.chat;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.BadRequestException;

public class DuplicateChatParticipant extends BadRequestException {
	public DuplicateChatParticipant(ErrorCode errorCode) {
		super(errorCode);
	}
}
