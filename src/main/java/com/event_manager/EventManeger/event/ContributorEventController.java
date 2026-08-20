package com.event_manager.EventManeger.event;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.event.dto.EventResponse;
import com.event_manager.EventManeger.user.CurrentUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contributor/events")
@RequiredArgsConstructor
public class ContributorEventController {

	private final EventService eventService;
	private final CurrentUserService currentUserService;

	@GetMapping
	public List<EventResponse> list(Authentication authentication) {
		return eventService.listForContributor(currentUserService.require(authentication));
	}

	@PostMapping("/{eventId}/applications")
	@ResponseStatus(HttpStatus.CREATED)
	public EventResponse apply(Authentication authentication, @PathVariable Long eventId) {
		return eventService.apply(currentUserService.require(authentication), eventId);
	}
}
