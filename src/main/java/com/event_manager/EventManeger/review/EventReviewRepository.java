package com.event_manager.EventManeger.review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.user.User;

public interface EventReviewRepository extends JpaRepository<EventReview, Long> {

	List<EventReview> findByRevieweeAndEventOrderByCreatedAtDesc(User reviewee, Event event);

	List<EventReview> findByRevieweeOrderByCreatedAtDesc(User reviewee);

	Optional<EventReview> findByEventAndReviewerAndReviewee(Event event, User reviewer, User reviewee);

	@Query("select avg(r.rating) from EventReview r where r.reviewee = :reviewee")
	Double averageRatingFor(@Param("reviewee") User reviewee);

	@Query("select count(r) from EventReview r where r.reviewee = :reviewee")
	long countFor(@Param("reviewee") User reviewee);
}
