package com.event_manager.EventManeger.review;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.common.ConflictException;
import com.event_manager.EventManeger.common.ForbiddenActionException;
import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.event.EventRepository;
import com.event_manager.EventManeger.event.EventService;
import com.event_manager.EventManeger.event.EventStatus;
import com.event_manager.EventManeger.review.dto.CreateReviewRequest;
import com.event_manager.EventManeger.review.dto.ReviewResponse;
import com.event_manager.EventManeger.review.dto.UserReviewsResponse;
import com.event_manager.EventManeger.user.Role;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserRepository;
import com.event_manager.EventManeger.user.UserSummary;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final EventReviewRepository reviewRepository;
	private final EventRepository eventRepository;
	private final EventService eventService;
	private final UserRepository userRepository;

	@Transactional
	public ReviewResponse submit(User reviewer, CreateReviewRequest request) {
		Event event = eventRepository.findById(request.eventId())
				.orElseThrow(() -> new NotFoundException("Event not found"));
		if (event.getStatus() != EventStatus.COMPLETED) {
			throw new ForbiddenActionException("Reviews are available after the organizer marks the event completed");
		}
		if (!eventService.participatedInEvent(reviewer, event)) {
			throw new ForbiddenActionException("You can only review people from events you participated in");
		}

		User reviewee = userRepository.findById(request.revieweeId())
				.orElseThrow(() -> new NotFoundException("User not found"));
		if (reviewee.getId().equals(reviewer.getId())) {
			throw new ForbiddenActionException("You cannot review yourself");
		}
		if (!eventService.participatedInEvent(reviewee, event)) {
			throw new ForbiddenActionException("This person did not participate in the event");
		}
		if (!canReview(reviewer, reviewee, event)) {
			throw new ForbiddenActionException("You cannot review this persona for this event");
		}
		if (reviewRepository.findByEventAndReviewerAndReviewee(event, reviewer, reviewee).isPresent()) {
			throw new ConflictException("You have already reviewed this person for this event");
		}

		EventReview review = new EventReview();
		review.setEvent(event);
		review.setReviewer(reviewer);
		review.setReviewee(reviewee);
		review.setRating(request.rating());
		review.setComment(blankToNull(request.comment()));
		return toResponse(reviewRepository.save(review));
	}

	@Transactional(readOnly = true)
	public UserReviewsResponse reviewsForUser(User viewer, Long userId) {
		User target = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User not found"));

		Optional<Event> lastSharedEvent = findLastSharedCompletedEvent(viewer, target);
		List<ReviewResponse> lastEventReviews = lastSharedEvent
				.map(event -> reviewRepository.findByRevieweeAndEventOrderByCreatedAtDesc(target, event).stream()
						.map(this::toResponse)
						.toList())
				.orElse(List.of());

		Double average = reviewRepository.averageRatingFor(target);
		long total = reviewRepository.countFor(target);

		return new UserReviewsResponse(
				target.getId(),
				target.getFullName(),
				average == null ? 0.0 : average,
				total,
				lastSharedEvent.map(Event::getId).orElse(null),
				lastSharedEvent.map(Event::getTitle).orElse(null),
				lastEventReviews);
	}

	private Optional<Event> findLastSharedCompletedEvent(User viewer, User target) {
		return eventRepository.findAll().stream()
				.filter(event -> event.getStatus() == EventStatus.COMPLETED)
				.filter(event -> eventService.participatedInEvent(viewer, event)
						&& eventService.participatedInEvent(target, event))
				.max(Comparator.comparing(Event::getUpdatedAt));
	}

	private boolean canReview(User reviewer, User reviewee, Event event) {
		Set<Role> reviewerRoles = eventService.rolesOnEvent(reviewer, event);
		Set<Role> revieweeRoles = eventService.rolesOnEvent(reviewee, event);

		if (reviewerRoles.contains(Role.ORGANIZER)) {
			return revieweeRoles.contains(Role.CONTRIBUTOR) || revieweeRoles.contains(Role.CREW);
		}
		if (reviewerRoles.contains(Role.CONTRIBUTOR)) {
			return revieweeRoles.contains(Role.ORGANIZER) || revieweeRoles.contains(Role.CREW);
		}
		if (reviewerRoles.contains(Role.CREW)) {
			return revieweeRoles.contains(Role.ORGANIZER) || revieweeRoles.contains(Role.CONTRIBUTOR);
		}
		return false;
	}

	private ReviewResponse toResponse(EventReview review) {
		return new ReviewResponse(
				review.getId(),
				UserSummary.from(review.getReviewer()),
				UserSummary.from(review.getReviewee()),
				review.getRating(),
				review.getComment(),
				review.getEvent().getId(),
				review.getEvent().getTitle(),
				review.getCreatedAt());
	}

	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
