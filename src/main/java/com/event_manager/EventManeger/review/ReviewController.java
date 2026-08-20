package com.event_manager.EventManeger.review;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.review.dto.CreateReviewRequest;
import com.event_manager.EventManeger.review.dto.ReviewResponse;
import com.event_manager.EventManeger.review.dto.UserReviewsResponse;
import com.event_manager.EventManeger.user.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;
	private final CurrentUserService currentUserService;

	@PostMapping
	public ReviewResponse submit(Authentication authentication, @Valid @RequestBody CreateReviewRequest request) {
		return reviewService.submit(currentUserService.require(authentication), request);
	}

	@GetMapping("/users/{userId}")
	public UserReviewsResponse forUser(Authentication authentication, @PathVariable Long userId) {
		return reviewService.reviewsForUser(currentUserService.require(authentication), userId);
	}
}
