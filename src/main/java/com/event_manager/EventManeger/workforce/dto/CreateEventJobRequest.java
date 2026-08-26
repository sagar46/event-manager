package com.event_manager.EventManeger.workforce.dto;

import java.time.Instant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEventJobRequest(
		@NotNull(message = "Crew role is required")
		Long crewRoleId,

		@Min(value = 1, message = "Required quantity must be at least 1")
		int requiredQuantity,

		Instant startsAt,
		Instant endsAt,

		@Size(max = 120)
		String location,

		@Size(max = 1000)
		String instructions) {
}
