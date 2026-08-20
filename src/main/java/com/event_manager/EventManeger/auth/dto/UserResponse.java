package com.event_manager.EventManeger.auth.dto;

import java.util.Set;

import com.event_manager.EventManeger.user.ProfileCompletion;
import com.event_manager.EventManeger.user.Role;
import com.event_manager.EventManeger.user.UserSummary;

public record UserResponse(
		Long id,
		String fullName,
		String email,
		Set<Role> roles,
		String phone,
		String city,
		String bio,
		String organization,
		UserSummary affiliatedContributor,
		ProfileCompletion profileCompletion) {
}
