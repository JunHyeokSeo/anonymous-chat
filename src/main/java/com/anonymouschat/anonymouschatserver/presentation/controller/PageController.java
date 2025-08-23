package com.anonymouschat.anonymouschatserver.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

	@GetMapping("/register")
	public String registerPage() {
		return "register";
	}

	@GetMapping("/")
	public String mainPage() {
		return "index";
	}

	@GetMapping("/chat/{roomId}")
	public String chatPage(@PathVariable Long roomId, Model model) {
		model.addAttribute("roomId", roomId);
		return "chat";
	}

	@GetMapping("/chats")
	public String chatPages() {
		return "chats";
	}

	@GetMapping("/profile")
	public String profilePage() {
		return "profile";
	}
}