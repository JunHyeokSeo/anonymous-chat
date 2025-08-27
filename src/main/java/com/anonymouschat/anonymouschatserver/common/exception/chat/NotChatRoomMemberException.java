package com.anonymouschat.anonymouschatserver.common.exception.chat;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.AbstractCustomException;

public class NotChatRoomMemberException extends AbstractCustomException {
    public NotChatRoomMemberException(ErrorCode errorCode) {
        super(errorCode);
    }
}