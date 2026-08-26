package com.event_manager.EventManeger.crew;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewRoleRepository extends JpaRepository<CrewRole, Long> {
	Optional<CrewRole> findByCode(String code);

	List<CrewRole> findByActiveTrueOrderByNameAsc();
}
