package com.anonymouschat.anonymouschatserver.presentation.controller;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.infra.web.HttpRequestExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
			description = "OAuth 로그인 성공 후 임시 토큰을 실제 토큰으로 교환하고 인증 처리를 완료합니다.",
			parameters = {
					@Parameter(
							name = "code",
							description = "OAuth 인증 성공 후 발급된 임시 인증 코드",
							required = true,
							example = "temp_auth_code_12345",
							schema = @Schema(type = "string")
					)
			},
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "인증 콜백 처리 성공 - auth-callback 페이지로 이동",
							content = @Content(
									mediaType = "text/html",
									examples = @ExampleObject(
											name = "성공적인 콜백 처리",
											description = "토큰 교환이 성공하고 auth-callback.html 페이지가 렌더링됩니다.",
											value = "auth-callback 템플릿 페이지 (토큰 데이터 포함)"
									)
							)
					),
					@ApiResponse(
							responseCode = "302",
							description = "리다이렉트 응답",
							content = @Content(
									mediaType = "text/html",
									examples = {
											@ExampleObject(
													name = "유효하지 않은 코드",
													description = "임시 코드가 만료되었거나 유효하지 않은 경우",
													value = "redirect:/login?error=invalid_code"
											),
											@ExampleObject(
													name = "처리 실패",
													description = "예상치 못한 오류가 발생한 경우",
													value = "redirect:/login?error=processing_failed"
											)
									}
							)
					)
			}
	)
	public String authCallback(
			@Parameter(description = "OAuth 임시 인증 코드") @RequestParam String code,
			Model model,
			HttpServletRequest request
	) {
		log.info("OAuth 콜백 처리 시작 - code: {}", code);

		try {
			AuthUseCaseDto.AuthTempData oldTokens = authUseCase.consumeOAuthTempData(code);
			AuthUseCaseDto.AuthTokens newTokens = authUseCase.refreshByUserId(oldTokens.userId(), httpRequestExtractor.extractUserAgent(request), httpRequestExtractor.extractClientIpAddress(request));
			log.info("새로운 토큰 발급 완료 - accessToken: {}", newTokens.accessToken());
			model.addAttribute("tokenData", newTokens);

			log.info("OAuth 콜백 처리 완료 - userId: {}, isGuest: {}", oldTokens.userId(), oldTokens.isGuestUser());

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