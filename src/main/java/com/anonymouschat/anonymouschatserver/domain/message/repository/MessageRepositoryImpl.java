package com.anonymouschat.anonymouschatserver.domain.message.repository;

import com.anonymouschat.anonymouschatserver.domain.message.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.message.entity.QMessage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Message> findMessagesAfterExitTimeWithCursor(Long chatRoomId, LocalDateTime lastExitedAt, Long lastMessageId, int limit) {
		QMessage m = QMessage.message;

		return queryFactory
				       .selectFrom(m)
				       .where(
						       m.chatRoom.id.eq(chatRoomId),
						       lastExitedAt != null ? m.sentAt.goe(lastExitedAt) : null,
						       lastMessageId != null ? m.id.lt(lastMessageId) : null
				       )
				       .orderBy(m.id.desc())
				       .limit(limit)
				       .fetch();
	}

	@Override
	public void updateMessagesAsRead(Long chatRoomId, Long userId) {
		QMessage m = QMessage.message;

		queryFactory
                    .update(m)
                    .set(m.isRead, true)
                    .where(
		                    m.chatRoom.id.eq(chatRoomId),
		                    m.sender.id.ne(userId),           // 내가 보낸 건 제외
		                    m.isRead.isFalse()                // 아직 읽지 않은 메시지
                    )
                    .execute();

		entityManager.clear();
	}
}

