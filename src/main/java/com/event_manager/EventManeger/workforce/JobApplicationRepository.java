package com.event_manager.EventManeger.workforce;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.event_manager.EventManeger.user.User;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
	List<JobApplication> findByEventJobOrderByCreatedAtDesc(EventJob eventJob);

	List<JobApplication> findByCrewUserOrderByCreatedAtDesc(User crewUser);

	Optional<JobApplication> findByEventJobAndCrewUser(EventJob eventJob, User crewUser);

	long countByEventJobAndStatus(EventJob eventJob, JobApplicationStatus status);

	long countByEventJob(EventJob eventJob);

	@Query("""
			select ja from JobApplication ja
			join fetch ja.eventJob job
			join fetch job.event event
			join fetch job.requiredRole
			join fetch event.organizer
			join fetch ja.crewUser crew
			where crew.affiliatedContributor = :contributor
			  and ja.status in :statuses
			order by ja.createdAt desc
			""")
	List<JobApplication> findForContributorWithStatuses(
			@Param("contributor") User contributor,
			@Param("statuses") List<JobApplicationStatus> statuses);

	@Query("""
			select ja from JobApplication ja
			join fetch ja.eventJob job
			join fetch job.event event
			join fetch job.requiredRole
			join fetch event.organizer
			join fetch ja.crewUser crew
			where ja.id = :id
			  and crew.affiliatedContributor = :contributor
			""")
	Optional<JobApplication> findByIdAndContributor(
			@Param("id") Long id,
			@Param("contributor") User contributor);

	@Query("""
			select ja from JobApplication ja
			join ja.eventJob job
			where ja.crewUser = :crew
			  and job.event.id = :eventId
			""")
	List<JobApplication> findByCrewAndEventId(
			@Param("crew") User crew,
			@Param("eventId") Long eventId);

	@Query("""
			select distinct job.event.id
			from JobApplication ja
			join ja.eventJob job
			where ja.crewUser = :crew
			""")
	List<Long> findEventIdsByCrew(@Param("crew") User crew);
}
