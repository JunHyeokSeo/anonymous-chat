package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.entity.QMessage;
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
	public List<Message> findMessagesAfterExitTimeWithCursor(Long roomId, LocalDateTime lastExitedAt, Long lastMessageId, int limit) {
		QMessage m = QMessage.message;

		return queryFactory
				       .selectFrom(m)
				       .where(
						       m.chatRoom.id.eq(roomId),
						       lastExitedAt != null ? m.sentAt.goe(lastExitedAt) : null,
						       lastMessageId != null ? m.id.lt(lastMessageId) : null
				       )
				       .orderBy(m.id.asc())
				       .limit(limit)
				       .fetch();
	}

	@Override
	public Long updateMessagesAsRead(Long roomId, Long userId) {
		QMessage m = QMessage.message;

		//읽을 메시지 ID 조회 (isRead = false, 내가 보낸 것 아님)
		List<Long> targetMessageIds = queryFactory
				                              .select(m.id)
				                              .from(m)
				                              .where(
						                              m.chatRoom.id.eq(roomId),
						                              m.sender.id.ne(userId),
						                              m.isRead.isFalse()
				                              )
				                              .fetch();

		if (targetMessageIds.isEmpty()) {
			return null;
		}

		//읽음 처리
		queryFactory
				.update(m)
				.set(m.isRead, true)
				.where(m.id.in(targetMessageIds))
				.execute();

		entityManager.clear();

		//마지막 메시지 ID 반환
		return targetMessageIds.stream().max(Long::compareTo).orElse(null);
	}

	@Override
	public Long findMaxReadMessageId(Long roomId, Long senderId) {
		QMessage m = QMessage.message;

		return queryFactory
				       .select(m.id.max())
				       .from(m)
				       .where(
						       m.chatRoom.id.eq(roomId),
						       m.sender.id.eq(senderId),
						       m.isRead.isTrue()
				       )
				       .fetchOne();
	}
}

