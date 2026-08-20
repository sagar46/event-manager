package com.event_manager.EventManeger.event;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.common.ConflictException;
import com.event_manager.EventManeger.common.ForbiddenActionException;
import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.event.dto.CreateEventRequest;
import com.event_manager.EventManeger.event.dto.CrewApplicationResponse;
import com.event_manager.EventManeger.event.dto.EventResponse;
import com.event_manager.EventManeger.user.Role;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserRepository;
import com.event_manager.EventManeger.user.UserSummary;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

	private static final List<EventStatus> CREW_VISIBLE_STATUSES = List.of(EventStatus.APPROVED, EventStatus.COMPLETED);

	private final EventRepository eventRepository;
	private final EventApplicationRepository applicationRepository;
	private final CrewApplicationRepository crewApplicationRepository;
	private final UserRepository userRepository;
	private final EventMapper eventMapper;

	@Transactional
	public EventResponse create(User organizer, CreateEventRequest request) {
		Event event = new Event();
		event.setTitle(request.title().trim());
		event.setDescription(blankToNull(request.description()));
		event.setLocation(request.location().trim());
		event.setStartsAt(request.startsAt());
		event.setOrganizer(organizer);
		event.setStatus(EventStatus.PENDING_APPROVAL);

		if (request.taggedContributorIds() != null) {
			for (Long contributorId : request.taggedContributorIds()) {
				event.getTaggedContributors().add(requireContributor(contributorId));
			}
		}

		return toOrganizerResponse(eventRepository.save(event), organizer);
	}

	@Transactional(readOnly = true)
	public List<EventResponse> listForOrganizer(User organizer) {
		return eventRepository.findByOrganizerOrderByCreatedAtDesc(organizer).stream()
				.map(event -> toOrganizerResponse(event, organizer))
				.toList();
	}

	@Transactional(readOnly = true)
	public EventResponse getForOrganizer(User organizer, Long eventId) {
		return toOrganizerResponse(requireOwnedEvent(organizer, eventId), organizer);
	}

	@Transactional
	public EventResponse tagContributor(User organizer, Long eventId, Long contributorId) {
		Event event = requireOwnedEvent(organizer, eventId);
		User contributor = requireContributor(contributorId);
		if (event.isTagged(contributor)) {
			throw new ConflictException("This contributor is already tagged on the event");
		}

		event.getTaggedContributors().add(contributor);
		applicationRepository.findByEventAndContributor(event, contributor).ifPresent(application -> {
			if (application.getStatus() == ApplicationStatus.PENDING) {
				application.setStatus(ApplicationStatus.APPROVED);
			}
		});

		return toOrganizerResponse(eventRepository.save(event), organizer);
	}

	@Transactional
	public EventResponse decideApplication(User organizer, Long eventId, Long applicationId, ApplicationStatus decision) {
		Event event = requireOwnedEvent(organizer, eventId);
		EventApplication application = applicationRepository.findByIdAndEvent(applicationId, event)
				.orElseThrow(() -> new NotFoundException("Application not found"));
		if (application.getStatus() != ApplicationStatus.PENDING) {
			throw new ConflictException("This application has already been reviewed");
		}
		application.setStatus(decision);
		applicationRepository.save(application);
		return toOrganizerResponse(event, organizer);
	}

	@Transactional
	public EventResponse completeEvent(User organizer, Long eventId) {
		Event event = requireOwnedEvent(organizer, eventId);
		if (event.getStatus() != EventStatus.APPROVED) {
			throw new ConflictException("Only approved events can be marked completed");
		}
		event.setStatus(EventStatus.COMPLETED);
		return toOrganizerResponse(eventRepository.save(event), organizer);
	}

	@Transactional(readOnly = true)
	public List<EventResponse> listPendingForAdmin() {
		return eventRepository.findByStatusOrderByCreatedAtDesc(EventStatus.PENDING_APPROVAL).stream()
				.map(event -> eventMapper.toAdminResponse(event, applications(event), crewApplications(event)))
				.toList();
	}

	@Transactional
	public EventResponse decideEvent(Long eventId, EventStatus status) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event not found"));
		if (event.getStatus() != EventStatus.PENDING_APPROVAL) {
			throw new ConflictException("Only events waiting for approval can be reviewed");
		}
		event.setStatus(status);
		return eventMapper.toAdminResponse(eventRepository.save(event), applications(event), crewApplications(event));
	}

	@Transactional(readOnly = true)
	public List<EventResponse> listForContributor(User contributor) {
		List<Event> events;
		if (contributor.getCity() == null || contributor.getCity().isBlank()) {
			events = eventRepository.findApprovedTaggedForContributor(EventStatus.APPROVED, contributor);
		} else {
			events = eventRepository.findVisibleToContributor(
					EventStatus.APPROVED,
					contributor.getCity().trim(),
					contributor);
		}

		return uniqueEvents(events).stream()
				.map(event -> eventMapper.toResponse(event, applications(event), crewApplications(event), contributor))
				.toList();
	}

	@Transactional
	public EventResponse apply(User contributor, Long eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event not found"));
		if (!event.isApproved()) {
			throw new ForbiddenActionException("This event is not open to contributors yet");
		}
		if (!canSeeEvent(contributor, event)) {
			throw new ForbiddenActionException("This event is not available in your location");
		}
		if (event.isTagged(contributor)) {
			throw new ConflictException("You are already tagged on this event and do not need to apply");
		}
		if (applicationRepository.existsByEventAndContributor(event, contributor)) {
			throw new ConflictException("You have already applied to this event");
		}

		EventApplication application = new EventApplication();
		application.setEvent(event);
		application.setContributor(contributor);
		application.setStatus(ApplicationStatus.PENDING);
		applicationRepository.save(application);

		return eventMapper.toResponse(event, applications(event), crewApplications(event), contributor);
	}

	@Transactional(readOnly = true)
	public List<EventResponse> listForCrew(User crewUser) {
		User crew = reload(crewUser);
		if (crew.getCity() == null || crew.getCity().isBlank()) {
			return List.of();
		}
		return eventRepository.findByLocationAndStatuses(CREW_VISIBLE_STATUSES, crew.getCity().trim()).stream()
				.filter(this::hasActiveContributors)
				.map(event -> eventMapper.toResponse(event, applications(event), crewApplications(event), crew))
				.toList();
	}

	@Transactional
	public EventResponse applyAsCrew(User crewUser, Long eventId) {
		User crew = reload(crewUser);
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException("Event not found"));
		if (!event.isOpenForCrew()) {
			throw new ForbiddenActionException("This event is not open to crew yet");
		}
		if (!canCrewSeeEvent(crew, event)) {
			throw new ForbiddenActionException("This event is not available in your location");
		}
		User affiliatedContributor = requireAffiliatedContributor(crew);
		if (!isContributorActiveOnEvent(event, affiliatedContributor)) {
			throw new ForbiddenActionException("Your contributor is not part of this event yet");
		}
		if (crewApplicationRepository.existsByEventAndCrew(event, crew)) {
			throw new ConflictException("You have already applied to this event");
		}

		CrewApplication application = new CrewApplication();
		application.setEvent(event);
		application.setCrew(crew);
		application.setReviewerContributor(affiliatedContributor);
		application.setStatus(ApplicationStatus.PENDING);
		crewApplicationRepository.save(application);

		return eventMapper.toResponse(event, applications(event), crewApplications(event), crew);
	}

	@Transactional(readOnly = true)
	public List<CrewApplicationResponse> listPendingCrewApplications(User contributor) {
		return crewApplicationRepository
				.findByReviewerContributorAndStatusOrderByCreatedAtDesc(contributor, ApplicationStatus.PENDING)
				.stream()
				.map(eventMapper::toCrewApplication)
				.toList();
	}

	@Transactional
	public CrewApplicationResponse decideCrewApplication(User contributor, Long applicationId, ApplicationStatus decision) {
		CrewApplication application = crewApplicationRepository.findByIdAndReviewerContributor(applicationId, contributor)
				.orElseThrow(() -> new NotFoundException("Crew application not found"));
		if (application.getStatus() != ApplicationStatus.PENDING) {
			throw new ConflictException("This application has already been reviewed");
		}
		application.setStatus(decision);
		return eventMapper.toCrewApplication(crewApplicationRepository.save(application));
	}

	@Transactional(readOnly = true)
	public List<UserSummary> listContributors() {
		return userRepository.findAllByRole(Role.CONTRIBUTOR).stream()
				.map(UserSummary::from)
				.toList();
	}

	private boolean canSeeEvent(User contributor, Event event) {
		if (event.isTagged(contributor)) {
			return true;
		}
		if (contributor.getCity() == null || contributor.getCity().isBlank()) {
			return false;
		}
		return event.getLocation().equalsIgnoreCase(contributor.getCity().trim());
	}

	private boolean canCrewSeeEvent(User crew, Event event) {
		if (crew.getCity() == null || crew.getCity().isBlank()) {
			return false;
		}
		return event.getLocation().equalsIgnoreCase(crew.getCity().trim());
	}

	private User requireAffiliatedContributor(User crew) {
		if (crew.getAffiliatedContributor() == null) {
			throw new ForbiddenActionException("Choose a contributor during signup before applying to events");
		}
		return crew.getAffiliatedContributor();
	}

	private boolean hasActiveContributors(Event event) {
		if (!event.getTaggedContributors().isEmpty()) {
			return true;
		}
		return applications(event).stream().anyMatch(application -> application.getStatus() == ApplicationStatus.APPROVED);
	}

	private boolean isContributorActiveOnEvent(Event event, User contributor) {
		if (event.isTagged(contributor)) {
			return true;
		}
		return applicationRepository.findByEventAndContributor(event, contributor)
				.map(application -> application.getStatus() == ApplicationStatus.APPROVED)
				.orElse(false);
	}

	private Event requireOwnedEvent(User organizer, Long eventId) {
		return eventRepository.findByIdAndOrganizer(eventId, organizer)
				.orElseThrow(() -> new NotFoundException("Event not found"));
	}

	private User requireContributor(Long contributorId) {
		return userRepository.findByIdAndRole(contributorId, Role.CONTRIBUTOR)
				.orElseThrow(() -> new NotFoundException("Contributor not found"));
	}

	private EventResponse toOrganizerResponse(Event event, User organizer) {
		return eventMapper.toResponse(event, applications(event), crewApplications(event), organizer);
	}

	private List<Event> uniqueEvents(List<Event> events) {
		Map<Long, Event> unique = new LinkedHashMap<>();
		for (Event event : events) {
			unique.put(event.getId(), event);
		}
		return new ArrayList<>(unique.values());
	}

	private List<EventApplication> applications(Event event) {
		return new ArrayList<>(applicationRepository.findByEventOrderByCreatedAtDesc(event));
	}

	private List<CrewApplication> crewApplications(Event event) {
		return new ArrayList<>(crewApplicationRepository.findByEventOrderByCreatedAtDesc(event));
	}

	private User reload(User user) {
		return userRepository.findById(user.getId())
				.orElseThrow(() -> new NotFoundException("User not found"));
	}

	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	public boolean participatedInEvent(User user, Event event) {
		if (event.getOrganizer().getId().equals(user.getId())) {
			return true;
		}
		if (user.getRoles().contains(Role.CONTRIBUTOR)) {
			return isContributorActiveOnEvent(event, user);
		}
		if (user.getRoles().contains(Role.CREW)) {
			return crewApplicationRepository.findByEventAndCrew(event, user)
					.map(application -> application.getStatus() == ApplicationStatus.APPROVED)
					.orElse(false);
		}
		return false;
	}

	public Set<Role> rolesOnEvent(User user, Event event) {
		Set<Role> roles = EnumSet.noneOf(Role.class);
		if (event.getOrganizer().getId().equals(user.getId())) {
			roles.add(Role.ORGANIZER);
		}
		if (user.getRoles().contains(Role.CONTRIBUTOR) && isContributorActiveOnEvent(event, user)) {
			roles.add(Role.CONTRIBUTOR);
		}
		if (user.getRoles().contains(Role.CREW)) {
			crewApplicationRepository.findByEventAndCrew(event, user)
					.filter(application -> application.getStatus() == ApplicationStatus.APPROVED)
					.ifPresent(application -> roles.add(Role.CREW));
		}
		return roles;
	}
}
