package com.event_manager.EventManeger.user;

import java.util.ArrayList;
import java.util.List;

public record ProfileCompletion(
		int percent,
		boolean complete,
		List<String> missingFields) {

	public static ProfileCompletion from(User user) {
		List<String> missing = new ArrayList<>();
		if (isBlank(user.getFullName())) {
			missing.add("fullName");
		}
		if (isBlank(user.getEmail())) {
			missing.add("email");
		}
		if (isBlank(user.getPhone())) {
			missing.add("phone");
		}
		if (isBlank(user.getCity())) {
			missing.add("city");
		}
		if (isBlank(user.getBio())) {
			missing.add("bio");
		}
		if (isBlank(user.getOrganization())) {
			missing.add("organization");
		}

		int total = 6;
		int filled = total - missing.size();
		int percent = (int) Math.round(filled * 100.0 / total);
		return new ProfileCompletion(percent, missing.isEmpty(), List.copyOf(missing));
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
