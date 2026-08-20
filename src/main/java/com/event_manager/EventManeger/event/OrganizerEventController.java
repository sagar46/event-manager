package com.event_manager.EventManeger.event;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.event.dto.CreateEventRequest;
import com.event_manager.EventManeger.event.dto.EventResponse;
import com.event_manager.EventManeger.event.dto.TagContributorRequest;
import com.event_manager.EventManeger.user.CurrentUserService;
import com.event_manager.EventManeger.user.UserSummary;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organizer")
@RequiredArgsConstructor
public class OrganizerEventController {

	private final EventService eventService;
	private final CurrentUserService currentUserService;

	@PostMapping("/events")
	@ResponseStatus(HttpStatus.CREATED)
	public EventResponse create(Authentication authentication, @Valid @RequestBody CreateEventRequest request) {
		return eventService.create(currentUserService.require(authentication), request);
	}

	@GetMapping("/events")
	public List<EventResponse> list(Authentication authentication) {
		return eventService.listForOrganizer(currentUserService.require(authentication));
	}

	@GetMapping("/events/{eventId}")
	public EventResponse get(Authentication authentication, @PathVariable Long eventId) {
		return eventService.getForOrganizer(currentUserService.require(authentication), eventId);
	}

	@PostMapping("/events/{eventId}/contributors")
	public EventResponse tag(
			Authentication authentication,
			@PathVariable Long eventId,
			@Valid @RequestBody TagContributorRequest request) {
		return eventService.tagContributor(
				currentUserService.require(authentication),
				eventId,
				request.contributorId());
	}

	@PostMapping("/events/{eventId}/applications/{applicationId}/approve")
	public EventResponse approveApplication(
			Authentication authentication,
			@PathVariable Long eventId,
			@PathVariable Long applicationId) {
		return eventService.decideApplication(
				currentUserService.require(authentication),
				eventId,
				applicationId,
				ApplicationStatus.APPROVED);
	}

	@PostMapping("/events/{eventId}/applications/{applicationId}/reject")
	public EventResponse rejectApplication(
			Authentication authentication,
			@PathVariable Long eventId,
			@PathVariable Long applicationId) {
		return eventService.decideApplication(
				currentUserService.require(authentication),
				eventId,
				applicationId,
				ApplicationStatus.REJECTED);
	}

	@PostMapping("/events/{eventId}/complete")
	public EventResponse complete(Authentication authentication, @PathVariable Long eventId) {
		return eventService.completeEvent(currentUserService.require(authentication), eventId);
	}

	@GetMapping("/contributors")
	public List<UserSummary> contributors() {
		return eventService.listContributors();
	}
}
