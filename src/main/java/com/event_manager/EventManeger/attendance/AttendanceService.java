package com.event_manager.EventManeger.attendance;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.attendance.dto.AttendanceResponse;
import com.event_manager.EventManeger.attendance.dto.MarkAttendanceRequest;
import com.event_manager.EventManeger.attendance.dto.SelfCheckInRequest;
import com.event_manager.EventManeger.common.ConflictException;
import com.event_manager.EventManeger.common.ForbiddenActionException;
import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.crew.CrewCapability;
import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.event.EventStatus;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserSummary;
import com.event_manager.EventManeger.workforce.AssignmentStatus;
import com.event_manager.EventManeger.workforce.CrewAssignment;
import com.event_manager.EventManeger.workforce.CrewAssignmentRepository;
import com.event_manager.EventManeger.workforce.EventAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

	private final EventAccessService eventAccessService;
	private final CrewAssignmentRepository assignmentRepository;
	private final AttendanceRepository attendanceRepository;
	private final AttendanceAuditRepository auditRepository;

	@Transactional
	public List<AttendanceResponse> ensureRoster(User actor, Long eventId) {
		Event event = eventAccessService.requireEvent(eventId);
		eventAccessService.requireCapability(actor, event, CrewCapability.VIEW_ASSIGNMENTS);
		List<AttendanceResponse> roster = new ArrayList<>();
		for (CrewAssignment assignment : assignmentRepository.findByEventAndStatusOrderByCreatedAtAsc(
				event, AssignmentStatus.ASSIGNED)) {
			Attendance attendance = attendanceRepository.findByAssignment(assignment)
					.orElseGet(() -> createPending(assignment));
			roster.add(toResponse(attendance));
		}
		return roster;
	}

	@Transactional
	public AttendanceResponse mark(
			User actor,
			Long eventId,
			Long assignmentId,
			MarkAttendanceRequest request) {
		Event event = eventAccessService.requireEvent(eventId);
		CrewAssignment assignment = requireActiveAssignment(event, assignmentId);
		Attendance attendance = attendanceRepository.findByAssignment(assignment)
				.orElseGet(() -> createPending(assignment));

		AttendanceStatus oldStatus = attendance.getStatus();
		if (oldStatus != AttendanceStatus.PENDING && oldStatus != request.status()) {
			eventAccessService.requireCapability(actor, event, CrewCapability.CORRECT_ATTENDANCE);
		} else {
			eventAccessService.requireCapability(actor, event, CrewCapability.MARK_ATTENDANCE);
		}

		attendance.setStatus(request.status());
		attendance.setRemarks(request.remarks());
		if (request.status() == AttendanceStatus.PRESENT || request.status() == AttendanceStatus.LATE) {
			if (attendance.getCheckInTime() == null) {
				attendance.setCheckInTime(Instant.now());
				attendance.setCheckInMethod(CheckInMethod.MANAGER_MANUAL);
			}
		}
		attendance = attendanceRepository.save(attendance);
		writeAudit(attendance, actor, oldStatus, request.status(), request.reason());
		return toResponse(attendance);
	}

	@Transactional
	public AttendanceResponse selfCheckIn(User crew, Long eventId, Long assignmentId, SelfCheckInRequest request) {
		Event event = eventAccessService.requireEvent(eventId);
		if (event.getStatus() != EventStatus.APPROVED) {
			throw new ConflictException("Check-in is only allowed for approved events");
		}
		CrewAssignment assignment = requireActiveAssignment(event, assignmentId);
		if (!assignment.getCrewUser().getId().equals(crew.getId())) {
			throw new ForbiddenActionException("You can only check in for your own assignment");
		}
		if (attendanceRepository.existsByAssignmentAndCheckInTimeIsNotNull(assignment)) {
			throw new ConflictException("Already checked in for this assignment");
		}
		if (event.isRequireLocationForCheckIn()
				&& (request == null || request.latitude() == null || request.longitude() == null)) {
			throw new ConflictException("Location is required for check-in on this event");
		}

		Instant now = Instant.now();
		Instant referenceStart = assignment.getAssignedStartsAt() != null
				? assignment.getAssignedStartsAt()
				: event.getStartsAt();
		if (referenceStart != null) {
			Instant windowOpen = referenceStart.minus(Duration.ofMinutes(event.getCheckInWindowMinutesBefore()));
			if (now.isBefore(windowOpen)) {
				throw new ConflictException("Check-in window has not opened yet");
			}
		}

		AttendanceStatus computed = AttendanceStatus.PRESENT;
		if (referenceStart != null) {
			Instant lateAfter = referenceStart.plus(Duration.ofMinutes(event.getCheckInGraceMinutes()));
			if (now.isAfter(lateAfter)) {
				computed = AttendanceStatus.LATE;
			}
		}

		Attendance attendance = attendanceRepository.findByAssignment(assignment)
				.orElseGet(() -> createPending(assignment));
		AttendanceStatus oldStatus = attendance.getStatus();
		attendance.setCheckInTime(now);
		attendance.setCheckInMethod(CheckInMethod.SELF_CHECK_IN);
		attendance.setStatus(computed);
		if (request != null) {
			attendance.setRemarks(request.remarks());
		}
		attendance = attendanceRepository.save(attendance);
		writeAudit(attendance, crew, oldStatus, computed, "Self check-in");
		return toResponse(attendance);
	}

	@Transactional(readOnly = true)
	public List<AttendanceResponse> listForEvent(User actor, Long eventId) {
		Event event = eventAccessService.requireEvent(eventId);
		eventAccessService.requireCapability(actor, event, CrewCapability.VIEW_ASSIGNMENTS);
		return attendanceRepository.findByEventOrderByCreatedAtAsc(event).stream()
				.map(this::toResponse)
				.toList();
	}

	private Attendance createPending(CrewAssignment assignment) {
		Attendance attendance = new Attendance();
		attendance.setAssignment(assignment);
		attendance.setEvent(assignment.getEvent());
		attendance.setCrewUser(assignment.getCrewUser());
		attendance.setStatus(AttendanceStatus.PENDING);
		return attendanceRepository.save(attendance);
	}

	private CrewAssignment requireActiveAssignment(Event event, Long assignmentId) {
		CrewAssignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new NotFoundException("Assignment not found"));
		if (!assignment.getEvent().getId().equals(event.getId())) {
			throw new NotFoundException("Assignment not found");
		}
		if (!assignment.isActive()) {
			throw new ConflictException("Only active assignments are eligible for attendance");
		}
		return assignment;
	}

	private void writeAudit(
			Attendance attendance,
			User actor,
			AttendanceStatus oldStatus,
			AttendanceStatus newStatus,
			String reason) {
		if (oldStatus == newStatus) {
			return;
		}
		AttendanceAudit audit = new AttendanceAudit();
		audit.setAttendance(attendance);
		audit.setChangedBy(actor);
		audit.setOldStatus(oldStatus);
		audit.setNewStatus(newStatus);
		audit.setReason(reason);
		auditRepository.save(audit);
	}

	private AttendanceResponse toResponse(Attendance attendance) {
		CrewAssignment assignment = attendance.getAssignment();
		return new AttendanceResponse(
				attendance.getId(),
				assignment.getId(),
				attendance.getEvent().getId(),
				UserSummary.from(attendance.getCrewUser()),
				assignment.getRole().getName(),
				attendance.getStatus(),
				attendance.getCheckInTime(),
				attendance.getCheckOutTime(),
				attendance.getCheckInMethod(),
				attendance.getCheckOutMethod(),
				attendance.getRemarks());
	}
}
