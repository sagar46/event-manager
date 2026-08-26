package com.event_manager.EventManeger.workforce;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.common.ForbiddenActionException;
import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.crew.CrewCapability;
import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.event.EventRepository;
import com.event_manager.EventManeger.user.Role;
import com.event_manager.EventManeger.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventAccessService {

	private final EventRepository eventRepository;
	private final CrewAssignmentRepository assignmentRepository;

	@Transactional(readOnly = true)
	public Event requireEvent(Long eventId) {
		return eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event not found"));
	}

	@Transactional(readOnly = true)
	public Event requireOwnedEvent(User organizer, Long eventId) {
		return eventRepository.findByIdAndOrganizer(eventId, organizer)
				.orElseThrow(() -> new NotFoundException("Event not found"));
	}

	/**
	 * Organizer always has full control. Assigned Manager/Supervisor get powers
	 * from their assignment's {@link com.event_manager.EventManeger.crew.CrewRole} capabilities.
	 * There is no separate platform MANAGER role.
	 */
	@Transactional(readOnly = true)
	public void requireCapability(User actor, Event event, CrewCapability capability) {
		if (event.getOrganizer().getId().equals(actor.getId()) || actor.getRoles().contains(Role.ADMIN)) {
			return;
		}
		requireAssignedCapability(actor, event, capability);
	}

	/** Capability check for assigned crew only — organizers are not auto-allowed. */
	@Transactional(readOnly = true)
	public void requireAssignedCapability(User actor, Event event, CrewCapability capability) {
		if (actor.getRoles().contains(Role.ADMIN)) {
			return;
		}
		boolean allowed = assignmentRepository.findByEventAndStatusOrderByCreatedAtAsc(event, AssignmentStatus.ASSIGNED)
				.stream()
				.filter(assignment -> assignment.getCrewUser().getId().equals(actor.getId()))
				.anyMatch(assignment -> assignment.getRole().hasCapability(capability));
		if (!allowed) {
			throw new ForbiddenActionException("You do not have " + capability + " for this event");
		}
	}
}
