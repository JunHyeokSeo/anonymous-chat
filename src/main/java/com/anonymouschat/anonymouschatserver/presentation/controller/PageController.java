package com.anonymouschat.anonymouschatserver.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

	@GetMapping("/login")
	public String loginPage() {
		return "/pages/login";
	}

	@GetMapping("/register")
	public String registerPage() {
		return "/pages/register";
	}

	@GetMapping("/")
	public String mainPage() {
		return "/pages/index";
	}

	@GetMapping("/chat/{roomId}")
	public String chatPage(@PathVariable Long roomId, Model model) {
		model.addAttribute("roomId", roomId);
		return "/pages/chat";
	}

	@GetMapping("/chat-list")
	public String chatPages() {
		return "/pages/chat-list";
	}

	@GetMapping("/profile")
	public String profilePage() {
		return "/pages/profile";
	}

	@GetMapping("/profile/edit")
	public String editProfilePage() {
		return "/pages/edit-profile";
	}

	@GetMapping("/user/{userId}")
	public String userProfilePage(@PathVariable Long userId, Model model) {
		model.addAttribute("userId", userId);
		return "/pages/user-profile";
	}
}