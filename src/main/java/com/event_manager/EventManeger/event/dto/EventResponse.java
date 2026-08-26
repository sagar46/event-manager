package com.event_manager.EventManeger.event.dto;

import java.time.Instant;
import java.util.List;

import com.event_manager.EventManeger.event.EventStatus;
import com.event_manager.EventManeger.user.UserSummary;

public record EventResponse(
		Long id,
		String title,
		String description,
		String location,
		Instant startsAt,
		Instant endsAt,
		Long categoryId,
		String categoryName,
		Long eventTypeId,
		String eventTypeName,
		Long feedbackFormId,
		int checkInGraceMinutes,
		int checkInWindowMinutesBefore,
		boolean requireLocationForCheckIn,
		EventStatus status,
		UserSummary organizer,
		List<UserSummary> taggedContributors,
		List<ApplicationResponse> applications,
		ApplicationResponse myApplication,
		List<CrewApplicationResponse> crewApplications,
		CrewApplicationResponse myCrewApplication,
		boolean tagged,
		boolean hasApplied,
		Instant createdAt) {
}
