package com.event_manager.EventManeger.event;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.event_manager.EventManeger.catalog.EventCategory;
import com.event_manager.EventManeger.catalog.EventType;
import com.event_manager.EventManeger.form.Form;
import com.event_manager.EventManeger.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(length = 1000)
	private String description;

	@Column(nullable = false, length = 80)
	private String location;

	private Instant startsAt;

	private Instant endsAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private EventCategory category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_type_id")
	private EventType eventType;

	/** Optional default feedback/survey form for this event (e.g. sampling). */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feedback_form_id")
	private Form feedbackForm;

	/** Minutes after job/event start still counted as PRESENT before LATE. */
	@Column(nullable = false)
	private int checkInGraceMinutes = 15;

	/** How many minutes before start self/QR check-in is allowed. */
	@Column(nullable = false)
	private int checkInWindowMinutesBefore = 60;

	@Column(nullable = false)
	private boolean requireLocationForCheckIn = false;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organizer_id", nullable = false)
	private User organizer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private EventStatus status = EventStatus.PENDING_APPROVAL;

	@ManyToMany
	@JoinTable(
			name = "event_tagged_contributors",
			joinColumns = @JoinColumn(name = "event_id"),
			inverseJoinColumns = @JoinColumn(name = "contributor_id"))
	private Set<User> taggedContributors = new HashSet<>();

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public boolean isApproved() {
		return status == EventStatus.APPROVED;
	}

	public boolean isCompleted() {
		return status == EventStatus.COMPLETED;
	}

	public boolean isOpenForCrew() {
		return status == EventStatus.APPROVED || status == EventStatus.COMPLETED;
	}

	public boolean isTagged(User contributor) {
		return taggedContributors.stream().anyMatch(user -> user.getId().equals(contributor.getId()));
	}
}
