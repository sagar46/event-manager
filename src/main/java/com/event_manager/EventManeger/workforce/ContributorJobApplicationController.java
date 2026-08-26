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

@RestController
@RequestMapping("/api/contributor/job-applications")
@RequiredArgsConstructor
public class ContributorJobApplicationController {

	private final WorkforceService workforceService;
	private final CurrentUserService currentUserService;

	@GetMapping
	public List<JobApplicationResponse> listTasks(Authentication authentication) {
		return workforceService.listTasksForContributor(currentUserService.require(authentication));
	}

	@GetMapping("/{applicationId}")
	public JobApplicationResponse getTask(
			Authentication authentication,
			@PathVariable Long applicationId) {
		return workforceService.getTaskForContributor(
				currentUserService.require(authentication),
				applicationId);
	}

	@PostMapping("/{applicationId}/approve")
	public JobApplicationResponse approve(
			Authentication authentication,
			@PathVariable Long applicationId) {
		return workforceService.decideByContributor(
				currentUserService.require(authentication),
				applicationId,
				JobApplicationStatus.APPROVED);
	}

	@PostMapping("/{applicationId}/reject")
	public JobApplicationResponse reject(
			Authentication authentication,
			@PathVariable Long applicationId) {
		return workforceService.decideByContributor(
				currentUserService.require(authentication),
				applicationId,
				JobApplicationStatus.REJECTED);
	}

	@PostMapping("/{applicationId}/waitlist")
	public JobApplicationResponse waitlist(
			Authentication authentication,
			@PathVariable Long applicationId) {
		return workforceService.decideByContributor(
				currentUserService.require(authentication),
				applicationId,
				JobApplicationStatus.SHORTLISTED);
	}
}
