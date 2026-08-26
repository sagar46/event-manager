package com.event_manager.EventManeger.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.auth.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserDirectoryService userDirectoryService;

	@GetMapping("/{userId}")
	public UserResponse getUser(Authentication authentication, @PathVariable Long userId) {
		return userDirectoryService.getPublicProfile(userId);
	}
}
