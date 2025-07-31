package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;

public record UserSearchRequest(
		Gender gender,
		Region region,
		Integer minAge,
		Integer maxAge,
		int page,
		int size
) {}
