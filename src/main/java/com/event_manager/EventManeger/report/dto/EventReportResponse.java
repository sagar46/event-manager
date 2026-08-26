package com.event_manager.EventManeger.report.dto;

import java.time.Instant;
import java.util.Map;

import com.event_manager.EventManeger.user.UserSummary;

public record EventReportResponse(
		Long id,
		Long eventId,
		UserSummary submittedBy,
		String remarks,
		Map<String, Object> metrics,
		Instant createdAt) {
}
