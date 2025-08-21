package com.anonymouschat.anonymouschatserver.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view")
public class ViewController {

	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}

	@GetMapping("/register")
	public String register() {
		return "auth/register";
	}

	@GetMapping("/users")
	public String users() {
		return "user/user-list";
	}

	@GetMapping("/users/{id}")
	public String userDetail(@PathVariable Long id, Model model) {
		model.addAttribute("userId", id);
		return "user/user-detail";
	}

	@GetMapping("/chatrooms")
	public String chatrooms() {
		return "chat/chatroom-list";
	}

	@GetMapping("/chatrooms/{roomId}")
	public String chatroom(@PathVariable Long roomId, Model model) {
		model.addAttribute("roomId", roomId);
		return "chat/chatroom";
	}
}
