package com.anonymouschat.anonymouschatserver.domain.user.repository;

import com.anonymouschat.anonymouschatserver.application.dto.QUserSearchResult;
import com.anonymouschat.anonymouschatserver.application.dto.UserSearchCondition;
import com.anonymouschat.anonymouschatserver.application.dto.UserSearchResult;
import com.anonymouschat.anonymouschatserver.domain.block.entity.QBlock;
import com.anonymouschat.anonymouschatserver.domain.user.entity.QUser;
import com.anonymouschat.anonymouschatserver.domain.user.entity.QUserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<UserSearchResult> searchUsers(Long currentUserId, UserSearchCondition cond, List<Long> blockedUserIds, Pageable pageable) {
		QUser user = QUser.user;
		QUserProfileImage image = QUserProfileImage.userProfileImage;

		List<UserSearchResult> results = queryFactory
				                                 .select(new QUserSearchResult(
						                                 user.id,
						                                 user.nickname,
						                                 user.gender,
						                                 user.age,
						                                 user.region,
						                                 image.imageUrl,
						                                 user.lastActiveAt
				                                 ))
				                                 .from(user)
				                                 .leftJoin(image)
				                                 .on(image.user.id.eq(user.id)
						                                     .and(image.isRepresentative.isTrue())
						                                     .and(image.deleted.isFalse()))
				                                 .where(
						                                 user.id.notIn(blockedUserIds),
						                                 excludeCurrentUser(currentUserId),
						                                 genderEquals(cond.gender()),
						                                 ageBetween(cond.minAge(), cond.maxAge()),
						                                 regionEquals(cond.region())
				                                 )
				                                 .orderBy(user.lastActiveAt.desc())
				                                 .offset(pageable.getOffset())
				                                 .limit(pageable.getPageSize() + 1)
				                                 .fetch();

		boolean hasNext = results.size() > pageable.getPageSize();
		if (hasNext) {
			results.removeLast();
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private BooleanExpression excludeCurrentUser(Long currentUserId) {
		return currentUserId != null ? QUser.user.id.ne(currentUserId) : null;
	}

	private BooleanExpression genderEquals(Gender gender) {
		return gender != null ? QUser.user.gender.eq(gender) : null;
	}

	private BooleanExpression ageBetween(Integer minAge, Integer maxAge) {
		QUser user = QUser.user;
		if (minAge != null && maxAge != null) {
			return user.age.between(minAge, maxAge);
		} else if (minAge != null) {
			return user.age.goe(minAge);
		} else if (maxAge != null) {
			return user.age.loe(maxAge);
		} else {
			return null;
		}
	}

	private BooleanExpression regionEquals(Region region) {
		return region != null ? QUser.user.region.eq(region) : null;
	}
}
