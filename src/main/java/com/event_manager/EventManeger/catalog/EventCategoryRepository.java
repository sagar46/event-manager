package com.event_manager.EventManeger.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventCategoryRepository extends JpaRepository<EventCategory, Long> {
	Optional<EventCategory> findByCode(String code);

	List<EventCategory> findByActiveTrueOrderByNameAsc();
}
