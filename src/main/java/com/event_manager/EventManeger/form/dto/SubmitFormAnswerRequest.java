package com.event_manager.EventManeger.form.dto;

import jakarta.validation.constraints.NotNull;

public record SubmitFormAnswerRequest(
		@NotNull Long questionId,
		String valueText,
		Double valueNumber,
		Boolean valueBoolean) {
}
