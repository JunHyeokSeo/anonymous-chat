package com.anonymouschat.anonymouschatserver.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAgeRange {
	String message() default "최소 나이는 최대 나이보다 작아야 합니다.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}

