package com.event_manager.EventManeger.event;

import java.util.List;

import org.springframework.stereotype.Component;

import com.event_manager.EventManeger.event.dto.ApplicationResponse;
import com.event_manager.EventManeger.event.dto.CrewApplicationResponse;
import com.event_manager.EventManeger.event.dto.EventResponse;
import com.event_manager.EventManeger.user.Role;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserSummary;

@Component
public class EventMapper {

	public EventResponse toResponse(
			Event event,
			List<EventApplication> applications,
			List<CrewApplication> crewApplications,
			User viewer) {
		ApplicationResponse myApplication = null;
		CrewApplicationResponse myCrewApplication = null;
		boolean tagged = false;

		if (viewer != null) {
			if (viewer.getRoles().contains(Role.CONTRIBUTOR)) {
				tagged = event.isTagged(viewer);
				myApplication = applications.stream()
						.filter(application -> application.getContributor().getId().equals(viewer.getId()))
						.findFirst()
						.map(this::toApplication)
						.orElse(null);
			}
			if (viewer.getRoles().contains(Role.CREW)) {
				myCrewApplication = crewApplications.stream()
						.filter(application -> application.getCrew().getId().equals(viewer.getId()))
						.findFirst()
						.map(this::toCrewApplication)
						.orElse(null);
			}
		}

		boolean includeContributorApplications = viewer != null && event.getOrganizer().getId().equals(viewer.getId());
		boolean includeCrewApplications = includeContributorApplications
				|| (viewer != null && viewer.getRoles().contains(Role.CONTRIBUTOR));

		return new EventResponse(
				event.getId(),
				event.getTitle(),
				event.getDescription(),
				event.getLocation(),
				event.getStartsAt(),
				event.getStatus(),
				UserSummary.from(event.getOrganizer()),
				event.getTaggedContributors().stream().map(UserSummary::from).toList(),
				includeContributorApplications ? applications.stream().map(this::toApplication).toList() : List.of(),
				myApplication,
				includeCrewApplications ? crewApplications.stream().map(this::toCrewApplication).toList() : List.of(),
				myCrewApplication,
				tagged,
				event.getCreatedAt());
	}

	public EventResponse toAdminResponse(
			Event event,
			List<EventApplication> applications,
			List<CrewApplication> crewApplications) {
		return new EventResponse(
				event.getId(),
				event.getTitle(),
				event.getDescription(),
				event.getLocation(),
				event.getStartsAt(),
				event.getStatus(),
				UserSummary.from(event.getOrganizer()),
				event.getTaggedContributors().stream().map(UserSummary::from).toList(),
				applications.stream().map(this::toApplication).toList(),
				null,
				crewApplications.stream().map(this::toCrewApplication).toList(),
				null,
				false,
				event.getCreatedAt());
	}

	public ApplicationResponse toApplication(EventApplication application) {
		return new ApplicationResponse(
				application.getId(),
				UserSummary.from(application.getContributor()),
				application.getStatus(),
				application.getCreatedAt());
	}

	public CrewApplicationResponse toCrewApplication(CrewApplication application) {
		return new CrewApplicationResponse(
				application.getId(),
				UserSummary.from(application.getCrew()),
				UserSummary.from(application.getReviewerContributor()),
				application.getStatus(),
				application.getCreatedAt());
	}
}
