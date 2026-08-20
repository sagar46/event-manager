package com.event_manager.EventManeger.event;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.user.User;

public interface EventApplicationRepository extends JpaRepository<EventApplication, Long> {

	List<EventApplication> findByEventOrderByCreatedAtDesc(Event event);

	List<EventApplication> findByContributorOrderByCreatedAtDesc(User contributor);

	Optional<EventApplication> findByEventAndContributor(Event event, User contributor);

	Optional<EventApplication> findByIdAndEvent(Long id, Event event);

	boolean existsByEventAndContributor(Event event, User contributor);
}
