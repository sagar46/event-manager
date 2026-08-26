package com.event_manager.EventManeger.attendance;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.attendance.dto.AttendanceResponse;
import com.event_manager.EventManeger.attendance.dto.MarkAttendanceRequest;
import com.event_manager.EventManeger.attendance.dto.SelfCheckInRequest;
import com.event_manager.EventManeger.user.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events/{eventId}/attendance")
@RequiredArgsConstructor
public class AttendanceController {

	private final AttendanceService attendanceService;
	private final CurrentUserService currentUserService;

	@PostMapping("/roster")
	public List<AttendanceResponse> ensureRoster(Authentication authentication, @PathVariable Long eventId) {
		return attendanceService.ensureRoster(currentUserService.require(authentication), eventId);
	}

	@GetMapping
	public List<AttendanceResponse> list(Authentication authentication, @PathVariable Long eventId) {
		return attendanceService.listForEvent(currentUserService.require(authentication), eventId);
	}

	@PostMapping("/assignments/{assignmentId}/mark")
	public AttendanceResponse mark(
			Authentication authentication,
			@PathVariable Long eventId,
			@PathVariable Long assignmentId,
			@Valid @RequestBody MarkAttendanceRequest request) {
		return attendanceService.mark(
				currentUserService.require(authentication), eventId, assignmentId, request);
	}

	@PostMapping("/assignments/{assignmentId}/check-in")
	public AttendanceResponse selfCheckIn(
			Authentication authentication,
			@PathVariable Long eventId,
			@PathVariable Long assignmentId,
			@Valid @RequestBody(required = false) SelfCheckInRequest request) {
		return attendanceService.selfCheckIn(
				currentUserService.require(authentication),
				eventId,
				assignmentId,
				request != null ? request : new SelfCheckInRequest(null, null, null));
	}
}
