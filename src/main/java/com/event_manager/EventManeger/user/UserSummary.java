package com.event_manager.EventManeger.user;

public record UserSummary(
		Long id,
		String fullName,
		String phone,
		String email,
		String city) {

	public static UserSummary from(User user) {
		return new UserSummary(user.getId(), user.getFullName(), user.getPhone(), user.getEmail(), user.getCity());
	}
}
