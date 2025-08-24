package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.QChatRoomServiceDto_SummaryResult;
import com.anonymouschat.anonymouschatserver.domain.entity.QChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.QMessage;
import com.anonymouschat.anonymouschatserver.domain.entity.QUserProfileImage;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom{
	private final JPAQueryFactory queryFactory;

	@Override
	public List<ChatRoomServiceDto.SummaryResult> findActiveChatRoomsByUser(Long userId) {
		QChatRoom cr = QChatRoom.chatRoom;
		QUserProfileImage img = QUserProfileImage.userProfileImage;

		return queryFactory
				       .select(new QChatRoomServiceDto_SummaryResult(
						       cr.id,
						       selectOpponentId(cr, userId),
						       selectOpponentNickname(cr, userId),
						       selectOpponentAge(cr, userId),
						       selectOpponentRegion(cr, userId),
						       selectOpponentProfileImageUrl(img),
						       cr.updatedAt,
						       selectLastMessageContent(cr),
						       selectUnreadCount(cr, userId)
				       ))
				       .from(cr)
				       .leftJoin(img).on(
						img.user.id.eq(selectOpponentId(cr, userId)),
						img.isRepresentative.isTrue(),
						img.deleted.isFalse()
				)
				       .where(
						       cr.isActive.isTrue(),
						       isParticipantStillIn(cr, userId)
				       )
				       .orderBy(cr.updatedAt.desc())
				       .fetch();
	}

	private BooleanExpression isParticipantStillIn(QChatRoom cr, Long userId) {
		return cr.user1.id.eq(userId).and(cr.exit.user1Exited.eq(false))
				       .or(cr.user2.id.eq(userId).and(cr.exit.user2Exited.eq(false)));
	}

	private Expression<Long> selectOpponentId(QChatRoom cr, Long userId) {
		return new CaseBuilder()
				       .when(cr.user1.id.eq(userId)).then(cr.user2.id)
				       .otherwise(cr.user1.id);
	}

	private Expression<String> selectOpponentNickname(QChatRoom cr, Long userId) {
		return new CaseBuilder()
				       .when(cr.user1.id.eq(userId)).then(cr.user2.nickname)
				       .otherwise(cr.user1.nickname);
	}

	private Expression<Integer> selectOpponentAge(QChatRoom cr, Long userId) {
		return new CaseBuilder()
				       .when(cr.user1.id.eq(userId)).then(cr.user2.age)
				       .otherwise(cr.user1.age);
	}

	private Expression<String> selectOpponentRegion(QChatRoom cr, Long userId) {
		return new CaseBuilder()
				       .when(cr.user1.id.eq(userId)).then(cr.user2.region.stringValue())
				       .otherwise(cr.user1.region.stringValue());
	}

	private Expression<String> selectOpponentProfileImageUrl(QUserProfileImage img) {
		return img.imageUrl;
	}

	private Expression<Long> selectUnreadCount(QChatRoom cr, Long userId) {
		QMessage msg = QMessage.message;
		return JPAExpressions
				       .select(msg.count())
				       .from(msg)
				       .where(
						       msg.chatRoom.id.eq(cr.id),
						       msg.sender.id.ne(userId),
						       msg.isRead.isFalse()
				       );
	}

	private Expression<String> selectLastMessageContent(QChatRoom cr) {
		QMessage msg = QMessage.message;
		return JPAExpressions
				       .select(msg.content)
				       .from(msg)
				       .where(msg.chatRoom.id.eq(cr.id))
				       .orderBy(msg.sentAt.desc())
				       .limit(1);
	}
}
