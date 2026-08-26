package com.event_manager.EventManeger.event;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.user.CurrentUserService;
import com.event_manager.EventManeger.user.UserDirectoryService;
import com.event_manager.EventManeger.user.UserSummary;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contributor/crew")
@RequiredArgsConstructor
public class ContributorCrewController {

	private final UserDirectoryService userDirectoryService;
	private final CurrentUserService currentUserService;

	@GetMapping
	public List<UserSummary> listCrew(Authentication authentication) {
		return userDirectoryService.listCrewForContributor(currentUserService.require(authentication));
	}
}
