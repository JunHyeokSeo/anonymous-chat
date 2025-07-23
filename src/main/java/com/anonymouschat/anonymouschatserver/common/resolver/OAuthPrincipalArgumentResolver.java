package com.anonymouschat.anonymouschatserver.common.resolver;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.jwt.JwtAuthenticationFilter;
import com.anonymouschat.anonymouschatserver.common.security.OAuthPrincipal;
import com.anonymouschat.anonymouschatserver.common.security.PrincipalType;
import com.anonymouschat.anonymouschatserver.common.security.annotation.ValidPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class OAuthPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(ValidPrincipal.class)
				       && OAuthPrincipal.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(@NonNull MethodParameter parameter,
	                              ModelAndViewContainer mavContainer,
	                              @NonNull NativeWebRequest webRequest,
	                              WebDataBinderFactory binderFactory) {

		HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();

		OAuthPrincipal principal = JwtAuthenticationFilter.extractPrincipal(request);
		if (principal == null) {
			throw new IllegalStateException(ErrorCode.UNAUTHORIZED.getMessage());
		}

		ValidPrincipal annotation = parameter.getParameterAnnotation(ValidPrincipal.class);
		if (annotation == null) {
			throw new IllegalStateException("@ValidPrincipal annotation is missing");
		}

		PrincipalType required = annotation.value();
		if (!principal.getType().equals(required)) {
			throw new IllegalStateException("잘못된 토큰 유형입니다. (필요: " + required + ", 현재: " + principal.getType() + ")");
		}

		return principal;
	}
}
