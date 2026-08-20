package com.event_manager.EventManeger.event;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.event.dto.CrewApplicationResponse;
import com.event_manager.EventManeger.user.CurrentUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contributor/crew-applications")
@RequiredArgsConstructor
public class ContributorCrewApplicationController {

	private final EventService eventService;
	private final CurrentUserService currentUserService;

	@GetMapping
	public List<CrewApplicationResponse> listPending(Authentication authentication) {
		return eventService.listPendingCrewApplications(currentUserService.require(authentication));
	}

	@PostMapping("/{applicationId}/approve")
	public CrewApplicationResponse approve(Authentication authentication, @PathVariable Long applicationId) {
		return eventService.decideCrewApplication(
				currentUserService.require(authentication),
				applicationId,
				ApplicationStatus.APPROVED);
	}

	@PostMapping("/{applicationId}/reject")
	public CrewApplicationResponse reject(Authentication authentication, @PathVariable Long applicationId) {
		return eventService.decideCrewApplication(
				currentUserService.require(authentication),
				applicationId,
				ApplicationStatus.REJECTED);
	}
}
