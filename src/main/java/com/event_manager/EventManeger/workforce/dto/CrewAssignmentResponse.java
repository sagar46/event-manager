package com.event_manager.EventManeger.workforce.dto;

import java.time.Instant;
import java.util.Set;

import com.event_manager.EventManeger.crew.CrewCapability;
import com.event_manager.EventManeger.user.UserSummary;
import com.event_manager.EventManeger.workforce.AssignmentStatus;

public record CrewAssignmentResponse(
		Long id,
		Long eventId,
		Long eventJobId,
		UserSummary crew,
		Long roleId,
		String roleCode,
		String roleName,
		Set<CrewCapability> capabilities,
		AssignmentStatus status,
		Instant assignedStartsAt,
		Instant assignedEndsAt,
		String assignedLocation,
		Instant createdAt) {
}
