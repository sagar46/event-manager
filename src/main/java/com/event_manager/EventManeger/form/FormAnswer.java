package com.event_manager.EventManeger.form;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "form_answers",
		uniqueConstraints = @UniqueConstraint(columnNames = {"response_id", "question_id"}))
@Getter
@Setter
@NoArgsConstructor
public class FormAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "response_id", nullable = false)
	private FormResponse response;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	private FormQuestion question;

	@Column(length = 2000)
	private String valueText;

	private Double valueNumber;

	private Boolean valueBoolean;
}
