package com.event_manager.EventManeger.event;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.event.dto.EventResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

	private final EventService eventService;

	@GetMapping
	public List<EventResponse> pending() {
		return eventService.listPendingForAdmin();
	}

	@PostMapping("/{eventId}/approve")
	public EventResponse approve(@PathVariable Long eventId) {
		return eventService.decideEvent(eventId, EventStatus.APPROVED);
	}

	@PostMapping("/{eventId}/reject")
	public EventResponse reject(@PathVariable Long eventId) {
		return eventService.decideEvent(eventId, EventStatus.REJECTED);
	}
}
