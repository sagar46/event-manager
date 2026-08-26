package com.event_manager.EventManeger.attendance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.workforce.CrewAssignment;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
	Optional<Attendance> findByAssignment(CrewAssignment assignment);

	List<Attendance> findByEventOrderByCreatedAtAsc(Event event);

	boolean existsByAssignmentAndCheckInTimeIsNotNull(CrewAssignment assignment);
}
