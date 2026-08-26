package com.event_manager.EventManeger.event.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEventRequest(
		@NotBlank(message = "Title is required")
		@Size(max = 120, message = "Title must be at most 120 characters")
		String title,

		@Size(max = 1000, message = "Description must be at most 1000 characters")
		String description,

		@NotBlank(message = "Location is required")
		@Size(max = 80, message = "Location must be at most 80 characters")
		String location,

		Instant startsAt,
		Instant endsAt,
		Long categoryId,
		Long eventTypeId,
		Long feedbackFormId,
		Integer checkInGraceMinutes,
		Integer checkInWindowMinutesBefore,
		Boolean requireLocationForCheckIn,

		List<Long> taggedContributorIds) {
}
