package com.event_manager.EventManeger.review.dto;

import java.time.Instant;

import com.event_manager.EventManeger.user.UserSummary;

public record ReviewResponse(
		Long id,
		UserSummary reviewer,
		UserSummary reviewee,
		int rating,
		String comment,
		Long eventId,
		String eventTitle,
		Instant createdAt) {
}
