package com.event_manager.EventManeger.event;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.event_manager.EventManeger.user.User;

public interface EventRepository extends JpaRepository<Event, Long> {

	List<Event> findByOrganizerOrderByCreatedAtDesc(User organizer);

	List<Event> findByStatusOrderByCreatedAtDesc(EventStatus status);

	@Query("""
			select distinct e from Event e
			left join e.taggedContributors tagged
			where e.status = :status
			  and (
			    lower(e.location) = lower(:location)
			    or tagged.id = :contributorId
			  )
			order by e.createdAt desc
			""")
	List<Event> findVisibleToContributor(
			@Param("status") EventStatus status,
			@Param("location") String location,
			@Param("contributorId") Long contributorId);

	@Query("""
			select distinct e from Event e
			join e.taggedContributors tagged
			where e.status = :status and tagged.id = :contributorId
			order by e.createdAt desc
			""")
	List<Event> findApprovedTaggedForContributor(
			@Param("status") EventStatus status,
			@Param("contributorId") Long contributorId);

	Optional<Event> findByIdAndOrganizer(Long id, User organizer);

	@Query("""
			select distinct e from Event e
			join e.taggedContributors tagged
			where e.status in :statuses
			  and tagged.id = :contributorId
			order by e.createdAt desc
			""")
	List<Event> findTaggedForContributorWithStatuses(
			@Param("statuses") List<EventStatus> statuses,
			@Param("contributorId") Long contributorId);
}
