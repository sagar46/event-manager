package com.event_manager.EventManeger.workforce;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.user.User;

public interface CrewAssignmentRepository extends JpaRepository<CrewAssignment, Long> {
	List<CrewAssignment> findByEventAndStatusOrderByCreatedAtAsc(Event event, AssignmentStatus status);

	List<CrewAssignment> findByEventOrderByCreatedAtAsc(Event event);

	List<CrewAssignment> findByCrewUserOrderByCreatedAtDesc(User crewUser);

	Optional<CrewAssignment> findByEventJobAndCrewUser(EventJob eventJob, User crewUser);

	long countByEventJobAndStatus(EventJob eventJob, AssignmentStatus status);

	boolean existsByEventAndCrewUserAndStatus(Event event, User crewUser, AssignmentStatus status);
}
