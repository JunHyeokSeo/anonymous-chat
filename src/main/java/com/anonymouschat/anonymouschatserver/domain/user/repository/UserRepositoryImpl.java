package com.anonymouschat.anonymouschatserver.domain.user.repository;

import com.anonymouschat.anonymouschatserver.application.dto.QUserSearchResult;
import com.anonymouschat.anonymouschatserver.application.dto.UserSearchCondition;
import com.anonymouschat.anonymouschatserver.application.dto.UserSearchResult;
import com.anonymouschat.anonymouschatserver.domain.user.entity.QUser;
import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom{
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<UserSearchResult> searchUsers(UserSearchCondition cond, Pageable pageable) {
		QUser user = QUser.user;

		List<UserSearchResult> content = queryFactory
				                                 .select(new QUserSearchResult(
						                                 user.id,
						                                 user.nickname,
						                                 user.gender,
						                                 user.age,
						                                 user.region,
						                                 user.profileImages.get(0).imageUrl,
						                                 user.lastActiveAt
				                                 ))
				                                 .from(user)
				                                 .where(
						                                 eqGender(cond.gender()),
						                                 betweenAge(cond.minAge(), cond.maxAge()),
						                                 eqRegion(cond.region())
				                                 )
				                                 .orderBy(user.lastActiveAt.desc())
				                                 .offset(pageable.getOffset())
				                                 .limit(pageable.getPageSize())
				                                 .fetch();

		return new PageImpl<>(content, pageable, content.size());
	}

	private BooleanExpression eqGender(Gender gender) {
		return gender != null ? QUser.user.gender.eq(gender) : null;
	}

	private BooleanExpression betweenAge(Integer minAge, Integer maxAge) {
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

	private BooleanExpression eqRegion(Region region) {
		return region != null ? QUser.user.region.eq(region) : null;
	}
}
