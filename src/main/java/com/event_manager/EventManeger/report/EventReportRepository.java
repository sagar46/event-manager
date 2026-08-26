package com.event_manager.EventManeger.report;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.event.Event;

public interface EventReportRepository extends JpaRepository<EventReport, Long> {
	List<EventReport> findByEventOrderByCreatedAtDesc(Event event);

	Optional<EventReport> findFirstByEventOrderByCreatedAtDesc(Event event);
}
