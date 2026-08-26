package com.event_manager.EventManeger.media;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.media.dto.EventMediaResponse;
import com.event_manager.EventManeger.media.dto.RegisterMediaRequest;
import com.event_manager.EventManeger.user.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events/{eventId}/media")
@RequiredArgsConstructor
public class EventMediaController {

	private final EventMediaService eventMediaService;
	private final CurrentUserService currentUserService;

	@PostMapping
	public EventMediaResponse register(
			Authentication authentication,
			@PathVariable Long eventId,
			@Valid @RequestBody RegisterMediaRequest request) {
		return eventMediaService.register(currentUserService.require(authentication), eventId, request);
	}

	@GetMapping
	public List<EventMediaResponse> list(@PathVariable Long eventId) {
		return eventMediaService.list(eventId);
	}
}
