package com.event_manager.EventManeger.review.dto;

import java.util.List;

public record UserReviewsResponse(
		Long userId,
		String fullName,
		double averageRating,
		long totalReviews,
		Long lastEventId,
		String lastEventTitle,
		List<ReviewResponse> lastEventReviews) {
}
