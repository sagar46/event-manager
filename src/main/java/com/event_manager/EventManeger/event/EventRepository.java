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
			    or tagged = :contributor
			  )
			order by e.createdAt desc
			""")
	List<Event> findVisibleToContributor(
			@Param("status") EventStatus status,
			@Param("location") String location,
			@Param("contributor") User contributor);

	@Query("""
			select distinct e from Event e
			join e.taggedContributors tagged
			where e.status = :status and tagged = :contributor
			order by e.createdAt desc
			""")
	List<Event> findApprovedTaggedForContributor(
			@Param("status") EventStatus status,
			@Param("contributor") User contributor);

	Optional<Event> findByIdAndOrganizer(Long id, User organizer);

	@Query("""
			select e from Event e
			where e.status in :statuses
			  and lower(e.location) = lower(:location)
			order by e.createdAt desc
			""")
	List<Event> findByLocationAndStatuses(
			@Param("statuses") List<EventStatus> statuses,
			@Param("location") String location);
}
