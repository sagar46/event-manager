package com.event_manager.EventManeger.attendance.dto;

import com.event_manager.EventManeger.attendance.AttendanceStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MarkAttendanceRequest(
		@NotNull(message = "Status is required")
		AttendanceStatus status,

		@Size(max = 500)
		String remarks,

		@Size(max = 500)
		String reason) {
}
