package com.anonymouschat.testsupport.fixtures;

import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public class MessageFixtures {

    public static Message createDummyMessage(Long messageId, Long chatRoomId, User sender, String content) {
        ChatRoom chatRoom = ChatRoom.builder().build();
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .build();
        ReflectionTestUtils.setField(message, "id", messageId);
        ReflectionTestUtils.setField(message, "sentAt", LocalDateTime.now());
        return message;
    }
}