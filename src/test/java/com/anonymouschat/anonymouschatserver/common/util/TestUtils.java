package com.anonymouschat.anonymouschatserver.common.util;

import com.anonymouschat.anonymouschatserver.domain.chatroom.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.message.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class TestUtils {
    public static User createUser(Long id) throws Exception {
        User user = User.builder()
            .provider(OAuthProvider.GOOGLE)
            .providerId("provider-id-" + id)
            .nickname("User" + id)
            .gender(Gender.MALE)
            .age(25)
            .region(Region.SEOUL)
            .bio("bio")
            .build();
        setId(user, id);
        return user;
    }

    public static ChatRoom createChatRoom(Long id, User user1, User user2) throws Exception {
        ChatRoom chatRoom = ChatRoom.builder()
            .user1(user1)
            .user2(user2)
            .build();
        setId(chatRoom, id);
        return chatRoom;
    }

    public static Message createMessage(Long id, ChatRoom chatRoom, User sender, String content) throws Exception {
        Message message = Message.builder()
            .chatRoom(chatRoom)
            .sender(sender)
            .content(content)
            .build();
        setId(message, id);
        return message;
    }

    public static void setId(Object target, Long id) throws Exception {
        Field idField = target.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(target, id);
    }
}