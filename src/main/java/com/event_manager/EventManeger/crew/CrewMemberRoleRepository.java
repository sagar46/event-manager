package com.event_manager.EventManeger.crew;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.user.User;

public interface CrewMemberRoleRepository extends JpaRepository<CrewMemberRole, Long> {
	List<CrewMemberRole> findByCrewUserOrderByPrimaryRoleDesc(User crewUser);

	boolean existsByCrewUserAndCrewRole(User crewUser, CrewRole crewRole);
}
