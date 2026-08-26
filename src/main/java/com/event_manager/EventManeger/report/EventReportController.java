package com.event_manager.EventManeger.report;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.report.dto.EventReportResponse;
import com.event_manager.EventManeger.user.CurrentUserService;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events/{eventId}/reports")
@RequiredArgsConstructor
public class EventReportController {

	private final EventReportService eventReportService;
	private final CurrentUserService currentUserService;

	@GetMapping("/preview")
	public Map<String, Object> preview(Authentication authentication, @PathVariable Long eventId) {
		return eventReportService.preview(currentUserService.require(authentication), eventId);
	}

	@PostMapping
	public EventReportResponse submit(
			Authentication authentication,
			@PathVariable Long eventId,
			@RequestBody(required = false) SubmitReportRequest request) {
		return eventReportService.submit(
				currentUserService.require(authentication),
				eventId,
				request != null ? request.remarks() : null);
	}

	@GetMapping("/latest")
	public EventReportResponse latest(@PathVariable Long eventId) {
		return eventReportService.latest(eventId);
	}

	public record SubmitReportRequest(@Size(max = 4000) String remarks) {
	}
}
