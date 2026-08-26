package com.event_manager.EventManeger.workforce;

import java.time.Instant;

import com.event_manager.EventManeger.crew.CrewRole;
import com.event_manager.EventManeger.event.Event;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "crew_assignments",
		uniqueConstraints = @UniqueConstraint(columnNames = {"event_job_id", "crew_user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CrewAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_job_id", nullable = false)
	private EventJob eventJob;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "crew_user_id", nullable = false)
	private User crewUser;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "crew_role_id", nullable = false)
	private CrewRole role;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_application_id")
	private JobApplication jobApplication;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AssignmentStatus status = AssignmentStatus.ASSIGNED;

	private Instant assignedStartsAt;

	private Instant assignedEndsAt;

	@Column(length = 120)
	private String assignedLocation;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	public boolean isActive() {
		return status == AssignmentStatus.ASSIGNED;
	}

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
}
