package com.event_manager.EventManeger.event.dto;

import java.time.Instant;

import com.event_manager.EventManeger.event.ApplicationStatus;
import com.event_manager.EventManeger.user.UserSummary;

public record CrewApplicationResponse(
		Long id,
		UserSummary crew,
		UserSummary reviewerContributor,
		ApplicationStatus status,
		Instant createdAt) {
}
