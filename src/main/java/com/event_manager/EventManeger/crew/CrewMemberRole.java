package com.event_manager.EventManeger.crew;

import java.time.Instant;

import com.event_manager.EventManeger.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Links a crew user to one or more {@link CrewRole} capabilities they can work as. */
@Entity
@Table(
		name = "crew_member_roles",
		uniqueConstraints = @UniqueConstraint(columnNames = {"crew_user_id", "crew_role_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CrewMemberRole {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "crew_user_id", nullable = false)
	private User crewUser;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "crew_role_id", nullable = false)
	private CrewRole crewRole;

	@Column(nullable = false)
	private boolean primaryRole = false;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}
}
