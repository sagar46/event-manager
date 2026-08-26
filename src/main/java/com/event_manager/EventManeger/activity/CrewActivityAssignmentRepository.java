package com.event_manager.EventManeger.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.workforce.CrewAssignment;

public interface CrewActivityAssignmentRepository extends JpaRepository<CrewActivityAssignment, Long> {
	List<CrewActivityAssignment> findByAssignment(CrewAssignment assignment);
}
