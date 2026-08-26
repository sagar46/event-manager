package com.event_manager.EventManeger.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.event.Event;

public interface EventActivityRepository extends JpaRepository<EventActivity, Long> {
	List<EventActivity> findByEventAndActiveTrueOrderByNameAsc(Event event);
}
