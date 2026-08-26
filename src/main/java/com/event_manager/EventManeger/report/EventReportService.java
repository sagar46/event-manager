package com.event_manager.EventManeger.report;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.attendance.AttendanceRepository;
import com.event_manager.EventManeger.attendance.AttendanceStatus;
import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.crew.CrewCapability;
import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.form.FormResponseRepository;
import com.event_manager.EventManeger.media.EventMediaRepository;
import com.event_manager.EventManeger.media.MediaType;
import com.event_manager.EventManeger.report.dto.EventReportResponse;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserSummary;
import com.event_manager.EventManeger.workforce.AssignmentStatus;
import com.event_manager.EventManeger.workforce.CrewAssignmentRepository;
import com.event_manager.EventManeger.workforce.EventAccessService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventReportService {

	private final EventAccessService eventAccessService;
	private final CrewAssignmentRepository assignmentRepository;
	private final AttendanceRepository attendanceRepository;
	private final FormResponseRepository formResponseRepository;
	private final EventMediaRepository mediaRepository;
	private final EventReportRepository reportRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Transactional(readOnly = true)
	public Map<String, Object> preview(User actor, Long eventId) {
		Event event = eventAccessService.requireEvent(eventId);
		eventAccessService.requireCapability(actor, event, CrewCapability.SUBMIT_REPORT);
		return aggregate(event);
	}

	@Transactional
	public EventReportResponse submit(User actor, Long eventId, String remarks) {
		Event event = eventAccessService.requireEvent(eventId);
		eventAccessService.requireCapability(actor, event, CrewCapability.SUBMIT_REPORT);
		Map<String, Object> metrics = aggregate(event);
		EventReport report = new EventReport();
		report.setEvent(event);
		report.setSubmittedBy(actor);
		report.setRemarks(remarks);
		try {
			report.setMetricsJson(objectMapper.writeValueAsString(metrics));
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not serialize report metrics", exception);
		}
		return toResponse(reportRepository.save(report), metrics);
	}

	@Transactional(readOnly = true)
	public EventReportResponse latest(Long eventId) {
		Event event = eventAccessService.requireEvent(eventId);
		EventReport report = reportRepository.findFirstByEventOrderByCreatedAtDesc(event)
				.orElseThrow(() -> new NotFoundException("No report submitted yet"));
		return toResponse(report, parseMetrics(report.getMetricsJson()));
	}

	private Map<String, Object> aggregate(Event event) {
		Map<String, Object> metrics = new LinkedHashMap<>();
		long assigned = assignmentRepository
				.findByEventAndStatusOrderByCreatedAtAsc(event, AssignmentStatus.ASSIGNED)
				.size();
		metrics.put("totalCrewAssigned", assigned);

		var attendances = attendanceRepository.findByEventOrderByCreatedAtAsc(event);
		metrics.put("totalPresent", attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count());
		metrics.put("totalLate", attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count());
		metrics.put("totalAbsent", attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT
				|| a.getStatus() == AttendanceStatus.NO_SHOW).count());
		metrics.put("totalFeedbackResponses", formResponseRepository.countByEvent(event));
		metrics.put("photosUploaded", mediaRepository.countByEventAndMediaType(event, MediaType.PHOTO));
		metrics.put("videosUploaded", mediaRepository.countByEventAndMediaType(event, MediaType.VIDEO));
		return metrics;
	}

	private EventReportResponse toResponse(EventReport report, Map<String, Object> metrics) {
		return new EventReportResponse(
				report.getId(),
				report.getEvent().getId(),
				UserSummary.from(report.getSubmittedBy()),
				report.getRemarks(),
				metrics,
				report.getCreatedAt());
	}

	private Map<String, Object> parseMetrics(String json) {
		if (json == null || json.isBlank()) {
			return Map.of();
		}
		try {
			return objectMapper.readValue(json, new TypeReference<>() {
			});
		} catch (JsonProcessingException exception) {
			return Map.of();
		}
	}
}
