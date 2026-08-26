package com.event_manager.EventManeger.media;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.event.Event;

public interface EventMediaRepository extends JpaRepository<EventMedia, Long> {
	List<EventMedia> findByEventOrderByUploadedAtDesc(Event event);

	long countByEventAndMediaType(Event event, MediaType mediaType);
}
