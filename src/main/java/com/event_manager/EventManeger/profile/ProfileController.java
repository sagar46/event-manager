package com.event_manager.EventManeger.profile;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.auth.dto.UpdateProfileRequest;
import com.event_manager.EventManeger.auth.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileService profileService;

	@PatchMapping
	public UserResponse update(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
		return profileService.update(authentication.getName(), request);
	}
}
