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
import com.event_manager.EventManeger.workforce.dto.ApplyToJobRequest;
import com.event_manager.EventManeger.workforce.dto.CrewAssignmentResponse;
import com.event_manager.EventManeger.workforce.dto.EventJobResponse;
import com.event_manager.EventManeger.workforce.dto.JobApplicationResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crew")
@RequiredArgsConstructor
public class CrewWorkforceController {

	private final WorkforceService workforceService;
	private final CurrentUserService currentUserService;

	@GetMapping("/events/{eventId}/jobs")
	public List<EventJobResponse> listJobs(Authentication authentication, @PathVariable Long eventId) {
		return workforceService.listJobsForEvent(eventId, currentUserService.require(authentication));
	}

	@PostMapping("/jobs/{jobId}/applications")
	public JobApplicationResponse apply(
			Authentication authentication,
			@PathVariable Long jobId,
			@Valid @RequestBody(required = false) ApplyToJobRequest request) {
		return workforceService.apply(
				currentUserService.require(authentication),
				jobId,
				request != null ? request : new ApplyToJobRequest(null));
	}

	@GetMapping("/events/{eventId}/assignments")
	public List<CrewAssignmentResponse> activeAssignments(@PathVariable Long eventId) {
		return workforceService.listActiveAssignments(eventId);
	}
}
