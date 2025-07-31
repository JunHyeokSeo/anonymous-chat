package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;

public record UserSearchCondition(
		Gender gender,
		Integer minAge,
		Integer maxAge,
		Region region
) {}

