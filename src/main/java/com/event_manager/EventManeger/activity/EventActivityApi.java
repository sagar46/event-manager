package com.event_manager.EventManeger.activity;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.user.CurrentUserService;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.workforce.CrewAssignment;
import com.event_manager.EventManeger.workforce.CrewAssignmentRepository;
import com.event_manager.EventManeger.workforce.EventAccessService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class EventActivityService {

	private final EventAccessService eventAccessService;
	private final EventActivityRepository activityRepository;
	private final CrewActivityAssignmentRepository activityAssignmentRepository;
	private final CrewAssignmentRepository assignmentRepository;

	@Transactional
	public EventActivityResponse create(User organizer, Long eventId, CreateActivityRequest request) {
		Event event = eventAccessService.requireOwnedEvent(organizer, eventId);
		EventActivity activity = new EventActivity();
		activity.setEvent(event);
		activity.setName(request.name().trim());
		activity.setDescription(request.description());
		activity = activityRepository.save(activity);
		return toResponse(activity);
	}

	@Transactional(readOnly = true)
	public List<EventActivityResponse> list(Long eventId) {
		Event event = eventAccessService.requireEvent(eventId);
		return activityRepository.findByEventAndActiveTrueOrderByNameAsc(event).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public void assignToCrew(User organizer, Long eventId, AssignActivityRequest request) {
		Event event = eventAccessService.requireOwnedEvent(organizer, eventId);
		EventActivity activity = activityRepository.findById(request.activityId())
				.orElseThrow(() -> new NotFoundException("Activity not found"));
		if (!activity.getEvent().getId().equals(event.getId())) {
			throw new NotFoundException("Activity not found");
		}
		CrewAssignment assignment = assignmentRepository.findById(request.assignmentId())
				.orElseThrow(() -> new NotFoundException("Assignment not found"));
		CrewActivityAssignment link = new CrewActivityAssignment();
		link.setActivity(activity);
		link.setAssignment(assignment);
		activityAssignmentRepository.save(link);
	}

	private EventActivityResponse toResponse(EventActivity activity) {
		return new EventActivityResponse(
				activity.getId(),
				activity.getEvent().getId(),
				activity.getName(),
				activity.getDescription(),
				activity.isActive());
	}
}

@RestController
@RequestMapping("/api/organizer/events/{eventId}/activities")
@RequiredArgsConstructor
class OrganizerActivityController {

	private final EventActivityService activityService;
	private final CurrentUserService currentUserService;

	@PostMapping
	public EventActivityResponse create(
			Authentication authentication,
			@PathVariable Long eventId,
			@RequestBody CreateActivityRequest request) {
		return activityService.create(currentUserService.require(authentication), eventId, request);
	}

	@GetMapping
	public List<EventActivityResponse> list(@PathVariable Long eventId) {
		return activityService.list(eventId);
	}

	@PostMapping("/assign")
	public void assign(
			Authentication authentication,
			@PathVariable Long eventId,
			@RequestBody AssignActivityRequest request) {
		activityService.assignToCrew(currentUserService.require(authentication), eventId, request);
	}
}

record CreateActivityRequest(
		@NotBlank @Size(max = 120) String name,
		@Size(max = 500) String description) {
}

record AssignActivityRequest(@NotNull Long activityId, @NotNull Long assignmentId) {
}

record EventActivityResponse(Long id, Long eventId, String name, String description, boolean active) {
}
