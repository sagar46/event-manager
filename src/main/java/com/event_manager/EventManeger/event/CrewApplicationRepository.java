package com.event_manager.EventManeger.event;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event_manager.EventManeger.user.User;

public interface CrewApplicationRepository extends JpaRepository<CrewApplication, Long> {

	List<CrewApplication> findByEventOrderByCreatedAtDesc(Event event);

	List<CrewApplication> findByReviewerContributorAndStatusOrderByCreatedAtDesc(
			User reviewerContributor,
			ApplicationStatus status);

	Optional<CrewApplication> findByEventAndCrew(Event event, User crew);

	Optional<CrewApplication> findByIdAndReviewerContributor(Long id, User reviewerContributor);

	boolean existsByEventAndCrew(Event event, User crew);
}
