package com.event_manager.EventManeger.media;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.activity.EventActivity;
import com.event_manager.EventManeger.activity.EventActivityRepository;
import com.event_manager.EventManeger.common.NotFoundException;
import com.event_manager.EventManeger.crew.CrewCapability;
import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.media.dto.EventMediaResponse;
import com.event_manager.EventManeger.media.dto.RegisterMediaRequest;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.user.UserSummary;
import com.event_manager.EventManeger.workforce.CrewAssignment;
import com.event_manager.EventManeger.workforce.CrewAssignmentRepository;
import com.event_manager.EventManeger.workforce.EventAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventMediaService {

	private final EventAccessService eventAccessService;
	private final EventMediaRepository mediaRepository;
	private final CrewAssignmentRepository assignmentRepository;
	private final EventActivityRepository activityRepository;

	@Transactional
	public EventMediaResponse register(User actor, Long eventId, RegisterMediaRequest request) {
		Event event = eventAccessService.requireEvent(eventId);
		eventAccessService.requireCapability(actor, event, CrewCapability.UPLOAD_MEDIA);

		EventMedia media = new EventMedia();
		media.setEvent(event);
		media.setUploadedBy(actor);
		media.setMediaType(request.mediaType());
		media.setStorageKey(request.storageKey());
		media.setContentType(request.contentType());
		media.setSizeBytes(request.sizeBytes());
		media.setCaption(request.caption());
		media.setLocation(request.location());
		media.setMetadataJson(request.metadataJson());

		if (request.assignmentId() != null) {
			CrewAssignment assignment = assignmentRepository.findById(request.assignmentId())
					.orElseThrow(() -> new NotFoundException("Assignment not found"));
			media.setAssignment(assignment);
		}
		if (request.activityId() != null) {
			EventActivity activity = activityRepository.findById(request.activityId())
					.orElseThrow(() -> new NotFoundException("Activity not found"));
			media.setActivity(activity);
		}
		return toResponse(mediaRepository.save(media));
	}

	@Transactional(readOnly = true)
	public List<EventMediaResponse> list(Long eventId) {
		Event event = eventAccessService.requireEvent(eventId);
		return mediaRepository.findByEventOrderByUploadedAtDesc(event).stream()
				.map(this::toResponse)
				.toList();
	}

	private EventMediaResponse toResponse(EventMedia media) {
		return new EventMediaResponse(
				media.getId(),
				media.getEvent().getId(),
				media.getAssignment() != null ? media.getAssignment().getId() : null,
				media.getActivity() != null ? media.getActivity().getId() : null,
				UserSummary.from(media.getUploadedBy()),
				media.getMediaType(),
				media.getStorageKey(),
				media.getContentType(),
				media.getSizeBytes(),
				media.getCaption(),
				media.getLocation(),
				media.getUploadedAt());
	}
}
