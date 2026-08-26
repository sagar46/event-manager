package com.event_manager.EventManeger.attendance;

import java.time.Instant;

import com.event_manager.EventManeger.event.Event;
import com.event_manager.EventManeger.user.User;
import com.event_manager.EventManeger.workforce.CrewAssignment;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "attendances",
		uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "assignment_id", nullable = false, unique = true)
	private CrewAssignment assignment;

	/** Denormalized for reporting queries. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	/** Denormalized for reporting queries. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "crew_user_id", nullable = false)
	private User crewUser;

	private Instant checkInTime;

	private Instant checkOutTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AttendanceStatus status = AttendanceStatus.PENDING;

	@Enumerated(EnumType.STRING)
	@Column(length = 32)
	private CheckInMethod checkInMethod;

	@Enumerated(EnumType.STRING)
	@Column(length = 32)
	private CheckInMethod checkOutMethod;

	@Column(length = 500)
	private String remarks;

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
}
