package com.event_manager.EventManeger.event.dto;

import java.time.Instant;

import com.event_manager.EventManeger.event.ApplicationStatus;
import com.event_manager.EventManeger.user.UserSummary;

public record ApplicationResponse(
		Long id,
		UserSummary contributor,
		ApplicationStatus status,
		Instant createdAt) {
}
