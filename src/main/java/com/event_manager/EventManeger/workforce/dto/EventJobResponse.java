package com.event_manager.EventManeger.workforce.dto;

import java.time.Instant;

import com.event_manager.EventManeger.workforce.JobApplicationStatus;
import com.event_manager.EventManeger.workforce.JobStatus;

public record EventJobResponse(
		Long id,
		Long eventId,
		Long crewRoleId,
		String crewRoleCode,
		String crewRoleName,
		int requiredQuantity,
		long assignedCount,
		long applicationCount,
		boolean organizerMayReviewApplicants,
		Instant startsAt,
		Instant endsAt,
		String location,
		JobStatus status,
		String instructions,
		Instant createdAt,
		JobApplicationStatus myApplicationStatus) {
}
