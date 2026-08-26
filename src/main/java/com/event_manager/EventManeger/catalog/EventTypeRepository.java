package com.event_manager.EventManeger.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {
	List<EventType> findByCategoryIdAndActiveTrueOrderByNameAsc(Long categoryId);

	Optional<EventType> findByCategoryIdAndCode(Long categoryId, String code);
}
