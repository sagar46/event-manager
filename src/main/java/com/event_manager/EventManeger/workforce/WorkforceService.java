package com.event_manager.EventManeger.workforce;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.common.ConflictException;
import com.event_manager.EventManeger.common.ForbiddenActionException;
import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.crew.CrewCapability;
import com.event_manager.EventManeger.crew.CrewRole;
import com.event_manager.EventManeger.crew.CrewRoleRepository;
import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.event.EventStatus;
import com.event_manager.EventManeger.user.Role;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserRepository;
import com.event_manager.EventManeger.user.UserSummary;
import com.event_manager.EventManeger.workforce.dto.ApplyToJobRequest;
import com.event_manager.EventManeger.workforce.dto.CreateEventJobRequest;
import com.event_manager.EventManeger.workforce.dto.CrewAssignmentResponse;
import com.event_manager.EventManeger.workforce.dto.EventJobResponse;
import com.event_manager.EventManeger.workforce.dto.JobApplicationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkforceService {

	private final EventAccessService eventAccessService;
	private final EventJobRepository eventJobRepository;
	private final JobApplicationRepository jobApplicationRepository;
	private final CrewAssignmentRepository assignmentRepository;
	private final CrewRoleRepository crewRoleRepository;
	private final UserRepository userRepository;

	@Transactional
	public EventJobResponse createJob(User organizer, Long eventId, CreateEventJobRequest request) {
		Event event = eventAccessService.requireOwnedEvent(organizer, eventId);
		CrewRole role = crewRoleRepository.findById(request.crewRoleId())
				.orElseThrow(() -> new NotFoundException("Crew role not found"));
		EventJob job = new EventJob();
		job.setEvent(event);
		job.setRequiredRole(role);
		job.setRequiredQuantity(request.requiredQuantity());
		job.setStartsAt(request.startsAt() != null ? request.startsAt() : event.getStartsAt());
		job.setEndsAt(request.endsAt() != null ? request.endsAt() : event.getEndsAt());
		job.setLocation(request.location() != null ? request.location() : event.getLocation());
		job.setInstructions(request.instructions());
		job.setStatus(JobStatus.OPEN);
		return toJobResponse(eventJobRepository.save(job));
	}

	@Transactional(readOnly = true)
	public List<EventJobResponse> listJobsForEvent(Long eventId) {
		return listJobsForEvent(eventId, null);
	}

	@Transactional(readOnly = true)
	public List<EventJobResponse> listJobsForEvent(Long eventId, User viewer) {
		Event event = eventAccessService.requireEvent(eventId);
		java.util.Map<Long, JobApplicationStatus> myStatusByJobId = java.util.Map.of();
		if (viewer != null) {
			myStatusByJobId = jobApplicationRepository.findByCrewAndEventId(viewer, eventId).stream()
					.collect(java.util.stream.Collectors.toMap(
							application -> application.getEventJob().getId(),
							JobApplication::getStatus,
							(left, right) -> left));
		}
		final java.util.Map<Long, JobApplicationStatus> statuses = myStatusByJobId;
		return eventJobRepository.findByEventOrderByCreatedAtAsc(event).stream()
				.map(job -> toJobResponse(job, statuses.get(job.getId())))
				.toList();
	}

	@Transactional
	public JobApplicationResponse apply(User crew, Long jobId, ApplyToJobRequest request) {
		if (!crew.getRoles().contains(Role.CREW) && !crew.getRoles().contains(Role.ADMIN)) {
			throw new ForbiddenActionException("Only crew members can apply to jobs");
		}
		User applicant = reloadCrew(crew);
		if (applicant.getAffiliatedContributor() == null) {
			throw new ForbiddenActionException("Link a contributor on your profile before applying to jobs");
		}
		EventJob job = eventJobRepository.findById(jobId)
				.orElseThrow(() -> new NotFoundException("Event job not found"));
		if (job.getStatus() == JobStatus.CANCELLED || job.getStatus() == JobStatus.CLOSED) {
			throw new ConflictException("This job is not open for applications");
		}
		if (job.getEvent().getStatus() != EventStatus.APPROVED) {
			throw new ConflictException("Applications are only open for approved events");
		}
		jobApplicationRepository.findByEventJobAndCrewUser(job, applicant).ifPresent(existing -> {
			throw new ConflictException("You already applied for this job");
		});
		JobApplication application = new JobApplication();
		application.setEventJob(job);
		application.setCrewUser(applicant);
		application.setStatus(JobApplicationStatus.APPLIED);
		application.setNote(request != null ? request.note() : null);
		// Applications are always accepted even when required quantity is already filled;
		// the affiliated contributor decides approve / reject / waitlist.
		return toApplicationResponse(jobApplicationRepository.save(application));
	}

	@Transactional(readOnly = true)
	public List<JobApplicationResponse> listTasksForContributor(User contributor) {
		requireContributor(contributor);
		return jobApplicationRepository
				.findForContributorWithStatuses(
						contributor,
						List.of(JobApplicationStatus.APPLIED, JobApplicationStatus.SHORTLISTED))
				.stream()
				.map(this::toApplicationResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public JobApplicationResponse getTaskForContributor(User contributor, Long applicationId) {
		requireContributor(contributor);
		JobApplication application = jobApplicationRepository
				.findByIdAndContributor(applicationId, contributor)
				.orElseThrow(() -> new NotFoundException("Application not found"));
		return toApplicationResponse(application);
	}

	@Transactional
	public JobApplicationResponse decideByContributor(
			User contributor,
			Long applicationId,
			JobApplicationStatus decision) {
		requireContributor(contributor);
		JobApplication application = jobApplicationRepository
				.findByIdAndContributor(applicationId, contributor)
				.orElseThrow(() -> new NotFoundException("Application not found"));
		return finalizeDecision(application, decision);
	}

	@Transactional(readOnly = true)
	public List<JobApplicationResponse> listApplicationsForJob(User actor, Long eventId, Long jobId) {
		Event event = eventAccessService.requireEvent(eventId);
		EventJob job = requireJob(event, jobId);
		assertCanReviewApplicants(actor, event, job);
		return jobApplicationRepository.findByEventJobOrderByCreatedAtDesc(job).stream()
				.map(this::toApplicationResponse)
				.toList();
	}

	@Transactional
	public JobApplicationResponse decideApplication(
			User actor,
			Long eventId,
			Long applicationId,
			JobApplicationStatus decision) {
		Event event = eventAccessService.requireEvent(eventId);
		JobApplication application = jobApplicationRepository.findById(applicationId)
				.orElseThrow(() -> new NotFoundException("Application not found"));
		if (!application.getEventJob().getEvent().getId().equals(event.getId())) {
			throw new NotFoundException("Application not found");
		}
		assertCanReviewApplicants(actor, event, application.getEventJob());
		return finalizeDecision(application, decision);
	}

	@Transactional(readOnly = true)
	public List<CrewAssignmentResponse> listAssignments(Long eventId) {
		Event event = eventAccessService.requireEvent(eventId);
		return assignmentRepository.findByEventOrderByCreatedAtAsc(event).stream()
				.map(this::toAssignmentResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<CrewAssignmentResponse> listActiveAssignments(Long eventId) {
		Event event = eventAccessService.requireEvent(eventId);
		return assignmentRepository.findByEventAndStatusOrderByCreatedAtAsc(event, AssignmentStatus.ASSIGNED).stream()
				.map(this::toAssignmentResponse)
				.toList();
	}

	private JobApplicationResponse finalizeDecision(JobApplication application, JobApplicationStatus decision) {
		if (application.getStatus() != JobApplicationStatus.APPLIED
				&& application.getStatus() != JobApplicationStatus.SHORTLISTED) {
			throw new ConflictException("Only applied/shortlisted applications can be decided");
		}
		if (decision != JobApplicationStatus.APPROVED
				&& decision != JobApplicationStatus.REJECTED
				&& decision != JobApplicationStatus.SHORTLISTED) {
			throw new ConflictException("Invalid decision");
		}
		if (decision == JobApplicationStatus.APPROVED) {
			EventJob job = application.getEventJob();
			long assigned = assignmentRepository.countByEventJobAndStatus(job, AssignmentStatus.ASSIGNED);
			if (assigned >= job.getRequiredQuantity()) {
				throw new ConflictException(
						"Required quantity is already filled. Use Waitlist for this application.");
			}
			application.setStatus(JobApplicationStatus.APPROVED);
			application = jobApplicationRepository.save(application);
			createAssignmentFromApplication(application);
			return toApplicationResponse(application);
		}
		application.setStatus(decision);
		return toApplicationResponse(jobApplicationRepository.save(application));
	}

	private void createAssignmentFromApplication(JobApplication application) {
		EventJob job = application.getEventJob();
		long assigned = assignmentRepository.countByEventJobAndStatus(job, AssignmentStatus.ASSIGNED);
		if (assigned >= job.getRequiredQuantity()) {
			throw new ConflictException("Required quantity for this job is already filled");
		}
		assignmentRepository.findByEventJobAndCrewUser(job, application.getCrewUser()).ifPresent(existing -> {
			throw new ConflictException("Crew member is already assigned to this job");
		});

		CrewAssignment assignment = new CrewAssignment();
		assignment.setEvent(job.getEvent());
		assignment.setEventJob(job);
		assignment.setCrewUser(application.getCrewUser());
		assignment.setRole(job.getRequiredRole());
		assignment.setJobApplication(application);
		assignment.setStatus(AssignmentStatus.ASSIGNED);
		assignment.setAssignedStartsAt(job.getStartsAt());
		assignment.setAssignedEndsAt(job.getEndsAt());
		assignment.setAssignedLocation(job.getLocation());
		assignmentRepository.save(assignment);

		if (assigned + 1 >= job.getRequiredQuantity()) {
			job.setStatus(JobStatus.FILLED);
			eventJobRepository.save(job);
		}
	}

	private User reloadCrew(User crew) {
		return userRepository.findById(crew.getId())
				.orElseThrow(() -> new NotFoundException("User not found"));
	}

	private void requireContributor(User user) {
		if (!user.getRoles().contains(Role.CONTRIBUTOR) && !user.getRoles().contains(Role.ADMIN)) {
			throw new ForbiddenActionException("Only contributors can review these applications");
		}
	}

	private EventJob requireJob(Event event, Long jobId) {
		return eventJobRepository.findByIdAndEvent(jobId, event)
				.orElseThrow(() -> new NotFoundException("Event job not found"));
	}

	private EventJobResponse toJobResponse(EventJob job) {
		return toJobResponse(job, null);
	}

	private EventJobResponse toJobResponse(EventJob job, JobApplicationStatus myApplicationStatus) {
		long assigned = assignmentRepository.countByEventJobAndStatus(job, AssignmentStatus.ASSIGNED);
		long applications = jobApplicationRepository.countByEventJob(job);
		boolean organizerMayReview = job.getRequiredRole().hasCapability(CrewCapability.VIEW_EVENT_CREW);
		return new EventJobResponse(
				job.getId(),
				job.getEvent().getId(),
				job.getRequiredRole().getId(),
				job.getRequiredRole().getCode(),
				job.getRequiredRole().getName(),
				job.getRequiredQuantity(),
				assigned,
				applications,
				organizerMayReview,
				job.getStartsAt(),
				job.getEndsAt(),
				job.getLocation(),
				job.getStatus(),
				job.getInstructions(),
				job.getCreatedAt(),
				myApplicationStatus);
	}

	/**
	 * Organizer may only inspect applicants for leadership jobs (Manager/Supervisor).
	 * Assigned managers review all other job applicants — organizers do not see who applied.
	 */
	private void assertCanReviewApplicants(User actor, Event event, EventJob job) {
		boolean leadershipJob = job.getRequiredRole().hasCapability(CrewCapability.VIEW_EVENT_CREW);
		if (actor.getRoles().contains(Role.ADMIN)) {
			return;
		}
		if (leadershipJob) {
			if (event.getOrganizer().getId().equals(actor.getId())) {
				return;
			}
			throw new ForbiddenActionException("Only the organizer can review Manager/Supervisor applicants");
		}
		eventAccessService.requireAssignedCapability(actor, event, CrewCapability.VIEW_EVENT_CREW);
	}

	private JobApplicationResponse toApplicationResponse(JobApplication application) {
		EventJob job = application.getEventJob();
		Event event = job.getEvent();
		long assigned = assignmentRepository.countByEventJobAndStatus(job, AssignmentStatus.ASSIGNED);
		long applications = jobApplicationRepository.countByEventJob(job);
		return new JobApplicationResponse(
				application.getId(),
				job.getId(),
				event.getId(),
				job.getRequiredRole().getName(),
				UserSummary.from(application.getCrewUser()),
				application.getStatus(),
				application.getNote(),
				application.getCreatedAt(),
				event.getTitle(),
				event.getLocation(),
				event.getStartsAt(),
				event.getStatus(),
				UserSummary.from(event.getOrganizer()),
				job.getRequiredQuantity(),
				assigned,
				applications,
				job.getStatus(),
				assigned < job.getRequiredQuantity());
	}

	private CrewAssignmentResponse toAssignmentResponse(CrewAssignment assignment) {
		return new CrewAssignmentResponse(
				assignment.getId(),
				assignment.getEvent().getId(),
				assignment.getEventJob().getId(),
				UserSummary.from(assignment.getCrewUser()),
				assignment.getRole().getId(),
				assignment.getRole().getCode(),
				assignment.getRole().getName(),
				assignment.getRole().getCapabilities(),
				assignment.getStatus(),
				assignment.getAssignedStartsAt(),
				assignment.getAssignedEndsAt(),
				assignment.getAssignedLocation(),
				assignment.getCreatedAt());
	}
}
