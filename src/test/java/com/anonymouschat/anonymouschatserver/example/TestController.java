package com.anonymouschat.anonymouschatserver.example;

import com.anonymouschat.anonymouschatserver.common.security.CustomPrincipal;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Profile("test")
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

	@GetMapping("/me")
	public String testMe(@AuthenticationPrincipal CustomPrincipal principal) {
		if (principal == null || principal.userId() == null) {
			throw new IllegalStateException("인증된 사용자만 호출 가능합니다.");
		}
		return "Hello, userId = " + principal.userId();
	}
}
