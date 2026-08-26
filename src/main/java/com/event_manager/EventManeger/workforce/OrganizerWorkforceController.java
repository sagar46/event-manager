package com.event_manager.EventManeger.workforce;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.user.CurrentUserService;
import com.event_manager.EventManeger.workforce.dto.CreateEventJobRequest;
import com.event_manager.EventManeger.workforce.dto.CrewAssignmentResponse;
import com.event_manager.EventManeger.workforce.dto.EventJobResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organizer/events/{eventId}")
@RequiredArgsConstructor
public class OrganizerWorkforceController {

	private final WorkforceService workforceService;
	private final CurrentUserService currentUserService;

	@PostMapping("/jobs")
	public EventJobResponse createJob(
			Authentication authentication,
			@PathVariable Long eventId,
			@Valid @RequestBody CreateEventJobRequest request) {
		return workforceService.createJob(currentUserService.require(authentication), eventId, request);
	}

	@GetMapping("/jobs")
	public List<EventJobResponse> listJobs(@PathVariable Long eventId) {
		return workforceService.listJobsForEvent(eventId);
	}

	@GetMapping("/assignments")
	public List<CrewAssignmentResponse> assignments(@PathVariable Long eventId) {
		return workforceService.listAssignments(eventId);
	}
}
