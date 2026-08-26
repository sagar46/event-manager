package com.event_manager.EventManeger.workforce;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.user.User;

public interface EventJobRepository extends JpaRepository<EventJob, Long> {
	List<EventJob> findByEventOrderByCreatedAtAsc(Event event);

	Optional<EventJob> findByIdAndEvent(Long id, Event event);
}
