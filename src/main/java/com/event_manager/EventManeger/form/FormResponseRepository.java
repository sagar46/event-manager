package com.event_manager.EventManeger.form;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.event.Event;

public interface FormResponseRepository extends JpaRepository<FormResponse, Long> {
	List<FormResponse> findByEventOrderByCreatedAtDesc(Event event);

	long countByEvent(Event event);
}
