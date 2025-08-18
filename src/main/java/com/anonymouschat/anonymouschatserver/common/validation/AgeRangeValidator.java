package com.anonymouschat.anonymouschatserver.common.validation;

import com.anonymouschat.anonymouschatserver.web.api.dto.UserControllerDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgeRangeValidator implements ConstraintValidator<ValidAgeRange, UserControllerDto.SearchConditionRequest> {

	@Override
	public boolean isValid(UserControllerDto.SearchConditionRequest value, ConstraintValidatorContext context) {
		if (value == null) {
			return true; // @NotNull 같은 제약은 필드에서 처리
		}
		if (value.minAge() == null || value.maxAge() == null) {
			return true; // 둘 중 하나만 입력된 경우는 허용
		}
		return value.minAge() <= value.maxAge();
	}
}

