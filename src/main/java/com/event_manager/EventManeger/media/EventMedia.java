package com.event_manager.EventManeger.media;

import java.time.Instant;

import com.event_manager.EventManeger.activity.EventActivity;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_media")
@Getter
@Setter
@NoArgsConstructor
public class EventMedia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignment_id")
	private CrewAssignment assignment;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "uploaded_by_user_id", nullable = false)
	private User uploadedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "activity_id")
	private EventActivity activity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private MediaType mediaType;

	@Column(nullable = false, length = 500)
	private String storageKey;

	@Column(length = 255)
	private String contentType;

	private Long sizeBytes;

	@Column(length = 500)
	private String caption;

	@Column(length = 120)
	private String location;

	@Column(length = 2000)
	private String metadataJson;

	@Column(nullable = false, updatable = false)
	private Instant uploadedAt;

	@PrePersist
	void onCreate() {
		uploadedAt = Instant.now();
	}
}
