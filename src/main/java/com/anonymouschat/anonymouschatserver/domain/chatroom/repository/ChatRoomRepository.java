package com.anonymouschat.anonymouschatserver.domain.chatroom.repository;

import com.anonymouschat.anonymouschatserver.domain.chatroom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom{
}
