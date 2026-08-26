package com.event_manager.EventManeger.form;

import java.time.Instant;

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
@Table(name = "form_questions")
@Getter
@Setter
@NoArgsConstructor
public class FormQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "form_id", nullable = false)
	private Form form;

	@Column(nullable = false, length = 500)
	private String prompt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private QuestionType questionType;

	/** JSON array of choices for SINGLE_CHOICE / MULTI_CHOICE, optional. */
	@Column(length = 2000)
	private String optionsJson;

	@Column(nullable = false)
	private boolean required = true;

	@Column(nullable = false)
	private int sortOrder = 0;

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
