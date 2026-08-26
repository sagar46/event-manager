package com.event_manager.EventManeger.workforce.dto;

import java.time.Instant;

import com.event_manager.EventManeger.event.EventStatus;
import com.event_manager.EventManeger.user.UserSummary;
import com.event_manager.EventManeger.workforce.JobApplicationStatus;
import com.event_manager.EventManeger.workforce.JobStatus;

public record JobApplicationResponse(
		Long id,
		Long eventJobId,
		Long eventId,
		String jobRoleName,
		UserSummary crew,
		JobApplicationStatus status,
		String note,
		Instant createdAt,
		String eventTitle,
		String eventLocation,
		Instant eventStartsAt,
		EventStatus eventStatus,
		UserSummary organizer,
		int requiredQuantity,
		long assignedCount,
		long applicationCount,
		JobStatus jobStatus,
		boolean slotsAvailable) {
}
