package com.event_manager.EventManeger.workforce;

import java.time.Instant;

import com.event_manager.EventManeger.crew.CrewRole;
import com.event_manager.EventManeger.event.Event;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_jobs")
@Getter
@Setter
@NoArgsConstructor
public class EventJob {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "crew_role_id", nullable = false)
	private CrewRole requiredRole;

	@Column(nullable = false)
	private int requiredQuantity = 1;

	private Instant startsAt;

	private Instant endsAt;

	@Column(length = 120)
	private String location;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private JobStatus status = JobStatus.OPEN;

	@Column(length = 1000)
	private String instructions;

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
