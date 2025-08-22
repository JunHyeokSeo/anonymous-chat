package com.anonymouschat.anonymouschatserver.presentation.controller;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.infra.web.HttpRequestExtractor;
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
public class AuthCallbackController {

	private final AuthUseCase authUseCase;
	private final HttpRequestExtractor httpRequestExtractor;

	@GetMapping("/callback")
	public String authCallback(@RequestParam String code, Model model, HttpServletRequest request) {
		log.info("OAuth 콜백 처리 시작 - code: {}", code);

		try {
			AuthUseCaseDto.OAuthTempData tokenData = authUseCase.consumeOAuthTempData(code);

			// Refresh Token이 있으면 저장 (게스트가 아닌 경우)
			if (tokenData.refreshToken() != null && tokenData.userId() != null) {
				String userAgent = httpRequestExtractor.extractUserAgent(request);
				String ipAddress = httpRequestExtractor.extractClientIpAddress(request);

				authUseCase.saveRefreshToken(tokenData.userId(), tokenData.refreshToken(), userAgent, ipAddress);
			}

			model.addAttribute("tokenData", tokenData);

			log.info("OAuth 콜백 처리 완료 - userId: {}, isGuest: {}",
					tokenData.userId(), tokenData.isGuestUser());

			return "auth-callback";

		} catch (InvalidTokenException e) {
			log.warn("유효하지 않은 임시 코드 - code: {}", code);
			return "redirect:/login?error=invalid_code";
		} catch (Exception e) {
			log.error("OAuth 콜백 처리 중 예상치 못한 오류", e);
			return "redirect:/login?error=processing_failed";
		}
	}
}