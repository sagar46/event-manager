package com.event_manager.EventManeger.form.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SubmitFormResponseRequest(
		@NotNull Long formId,
		Long assignmentId,
		Long activityId,
		@NotEmpty @Valid List<SubmitFormAnswerRequest> answers) {
}
