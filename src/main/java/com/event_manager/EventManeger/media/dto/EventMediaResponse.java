package com.event_manager.EventManeger.media.dto;

import java.time.Instant;

import com.event_manager.EventManeger.media.MediaType;
import com.event_manager.EventManeger.user.UserSummary;

public record EventMediaResponse(
		Long id,
		Long eventId,
		Long assignmentId,
		Long activityId,
		UserSummary uploadedBy,
		MediaType mediaType,
		String storageKey,
		String contentType,
		Long sizeBytes,
		String caption,
		String location,
		Instant uploadedAt) {
}
