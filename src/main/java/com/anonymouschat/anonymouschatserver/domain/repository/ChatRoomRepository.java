package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {

	@Query("""
        select cr from ChatRoom cr
        where cr.pairLeftId = :left and cr.pairRightId = :right and (cr.isActive = true or (cr.isActive = false and cr.exit.user1Exited = false or cr.exit.user2Exited = false)) order by cr.updatedAt desc limit 1
        """)
	Optional<ChatRoom> findLatestValidChatRoomByPair(@Param("left") long left, @Param("right") long right);

	@Query("""
        select (count(cr) > 0)
        from ChatRoom cr
        where cr.id = :roomId
          and (cr.user1.id = :participantId or cr.user2.id = :participantId)
        """)
	boolean existsByIdAndParticipantId(@Param("roomId") Long roomId,
	                                            @Param("participantId") Long participantId);
}
