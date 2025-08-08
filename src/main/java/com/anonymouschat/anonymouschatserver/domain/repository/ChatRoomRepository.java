package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.type.ChatRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom{
	@Query("""
        select (count(cr) > 0)
        from ChatRoom cr
        where cr.id = :chatRoomId
          and cr.status = :status
          and (cr.user1.id = :participantId or cr.user2.id = :participantId)
        """)
	boolean existsByIdAndParticipantIdAndStatus(@Param("chatRoomId") Long chatRoomId,
	                                            @Param("participantId") Long participantId,
	                                            @Param("status") ChatRoomStatus status);
}
