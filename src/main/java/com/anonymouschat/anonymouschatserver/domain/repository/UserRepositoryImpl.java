package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.application.dto.QUserServiceDto_SearchResult;
import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.domain.entity.QUser;
import com.anonymouschat.anonymouschatserver.domain.entity.QUserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.querydsl.core.types.dsl.BooleanExpression;
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
	public Slice<UserServiceDto.SearchResult> findUsersByCondition(UserServiceDto.SearchCommand cond, Pageable pageable) {
		QUser user = QUser.user;
		QUserProfileImage image = QUserProfileImage.userProfileImage;

		List<UserServiceDto.SearchResult> results = queryFactory
				                                 .select(new QUserServiceDto_SearchResult(
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
						                                 user.id.notIn(cond.blockedUserIds()),
						                                 excludeCurrentUser(cond.id()),
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
