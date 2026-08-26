package com.event_manager.EventManeger.attendance;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attendance_audits")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "attendance_id", nullable = false)
	private Attendance attendance;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "changed_by_user_id", nullable = false)
	private User changedBy;

	@Enumerated(EnumType.STRING)
	@Column(length = 32)
	private AttendanceStatus oldStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AttendanceStatus newStatus;

	@Column(length = 500)
	private String reason;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}
}
