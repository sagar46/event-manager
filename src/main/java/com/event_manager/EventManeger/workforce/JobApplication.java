package com.event_manager.EventManeger.workforce;

import java.time.Instant;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "job_applications",
		uniqueConstraints = @UniqueConstraint(columnNames = {"event_job_id", "crew_user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class JobApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_job_id", nullable = false)
	private EventJob eventJob;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "crew_user_id", nullable = false)
	private User crewUser;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private JobApplicationStatus status = JobApplicationStatus.APPLIED;

	@Column(length = 500)
	private String note;

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
