package com.event_manager.EventManeger.workforce;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.user.CurrentUserService;
import com.event_manager.EventManeger.workforce.dto.JobApplicationResponse;

import lombok.RequiredArgsConstructor;

/**
 * Applicant review for assigned Managers/Supervisors (and organizer for leadership jobs).
 * Path is authenticated (any role); service enforces event-scoped capabilities.
 */
@RestController
@RequestMapping("/api/events/{eventId}")
@RequiredArgsConstructor
public class EventJobApplicationController {

	private final WorkforceService workforceService;
	private final CurrentUserService currentUserService;

	@GetMapping("/jobs/{jobId}/job-applications")
	public List<JobApplicationResponse> listApplications(
			Authentication authentication,
			@PathVariable Long eventId,
			@PathVariable Long jobId) {
		return workforceService.listApplicationsForJob(
				currentUserService.require(authentication), eventId, jobId);
	}

	@PostMapping("/job-applications/{applicationId}/shortlist")
	public JobApplicationResponse shortlist(
			Authentication authentication,
			@PathVariable Long eventId,
			@PathVariable Long applicationId) {
		return workforceService.decideApplication(
				currentUserService.require(authentication),
				eventId,
				applicationId,
				JobApplicationStatus.SHORTLISTED);
	}

	@PostMapping("/job-applications/{applicationId}/approve")
	public JobApplicationResponse approve(
			Authentication authentication,
			@PathVariable Long eventId,
			@PathVariable Long applicationId) {
		return workforceService.decideApplication(
				currentUserService.require(authentication),
				eventId,
				applicationId,
				JobApplicationStatus.APPROVED);
	}

	@PostMapping("/job-applications/{applicationId}/reject")
	public JobApplicationResponse reject(
			Authentication authentication,
			@PathVariable Long eventId,
			@PathVariable Long applicationId) {
		return workforceService.decideApplication(
				currentUserService.require(authentication),
				eventId,
				applicationId,
				JobApplicationStatus.REJECTED);
	}
}
