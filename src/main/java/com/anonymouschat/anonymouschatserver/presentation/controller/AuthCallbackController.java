package com.anonymouschat.anonymouschatserver.presentation.controller;

import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.infra.web.HttpRequestExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Callback", description = "OAuth 인증 콜백 처리 API")
public class AuthCallbackController {

	private final AuthUseCase authUseCase;
	private final HttpRequestExtractor httpRequestExtractor;

	@GetMapping("/callback")
	@Operation(
			summary = "OAuth 인증 콜백 처리",
			description = "OAuth 로그인 성공 후 임시 토큰을 실제 토큰으로 교환합니다."
	)
	@ApiResponse(responseCode = "200", description = "콜백 성공 (auth-callback.html 렌더링)")
	@ApiResponse(responseCode = "302", description = "리다이렉트 (코드 만료 또는 오류)")
	public String authCallback(
			@Parameter(description = "OAuth 임시 인증 코드", required = true, example = "temp_auth_code_12345")
			@RequestParam String code,
			Model model,
			HttpServletRequest request
	) {
		log.info("OAuth 콜백 처리 시작 - code: {}", code);

		try {
			String userAgent = httpRequestExtractor.extractUserAgent(request);
			String ipAddress = httpRequestExtractor.extractClientIpAddress(request);

			var authData = authUseCase.handleOAuthCallback(code, userAgent, ipAddress);
			model.addAttribute("authData", authData);
			return "pages/auth-callback";
		} catch (InvalidTokenException e) {
			log.warn("유효하지 않은 임시 코드 - code: {}", code);
			return "redirect:/login?error=invalid_code";
		} catch (Exception e) {
			log.error("OAuth 콜백 처리 중 오류", e);
			return "redirect:/login?error=processing_failed";
		}
	}
}
