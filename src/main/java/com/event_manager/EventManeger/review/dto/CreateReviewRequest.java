package com.event_manager.EventManeger.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
		@NotNull(message = "Event is required")
		Long eventId,

		@NotNull(message = "Reviewee is required")
		Long revieweeId,

		@NotNull(message = "Rating is required")
		@Min(value = 1, message = "Rating must be at least 1")
		@Max(value = 5, message = "Rating must be at most 5")
		Integer rating,

		@Size(max = 500, message = "Comment must be at most 500 characters")
		String comment) {
}
