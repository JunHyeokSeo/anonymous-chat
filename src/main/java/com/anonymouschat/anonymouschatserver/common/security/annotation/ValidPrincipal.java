package com.anonymouschat.anonymouschatserver.common.security.annotation;

import com.anonymouschat.anonymouschatserver.common.security.PrincipalType;

import java.lang.annotation.*;

/**
 * 인증된 OAuthPrincipal을 주입받고, 요구되는 PrincipalType과 일치하는지 검증합니다.
 *
 * 이 어노테이션은 @AuthenticationPrincipal과 함께 사용되어야 합니다.
 * 예시:
 * <pre>
 *     @ValidPrincipal(PrincipalType.ACCESS)
 *     @AuthenticationPrincipal UserPrincipal principal
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidPrincipal {
	PrincipalType value(); // REQUIRED 토큰 목적
}
