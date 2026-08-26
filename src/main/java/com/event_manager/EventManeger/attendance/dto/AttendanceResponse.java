package com.event_manager.EventManeger.attendance.dto;

import java.time.Instant;

import com.event_manager.EventManeger.attendance.AttendanceStatus;
import com.event_manager.EventManeger.attendance.CheckInMethod;
import com.event_manager.EventManeger.user.UserSummary;

public record AttendanceResponse(
		Long id,
		Long assignmentId,
		Long eventId,
		UserSummary crew,
		String roleName,
		AttendanceStatus status,
		Instant checkInTime,
		Instant checkOutTime,
		CheckInMethod checkInMethod,
		CheckInMethod checkOutMethod,
		String remarks) {
}
