package com.event_manager.EventManeger.user;

import org.springframework.stereotype.Component;

import com.event_manager.EventManeger.auth.dto.UserResponse;

@Component
public class UserMapper {

	public UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getRoles(),
				user.getPhone(),
				user.getCity(),
				user.getBio(),
				user.getOrganization(),
				user.getAffiliatedContributor() == null ? null : UserSummary.from(user.getAffiliatedContributor()),
				ProfileCompletion.from(user));
	}
}
