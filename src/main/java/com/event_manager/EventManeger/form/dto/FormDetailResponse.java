package com.event_manager.EventManeger.form.dto;

import java.util.List;

public record FormDetailResponse(
		Long id,
		String name,
		String description,
		List<FormQuestionResponse> questions) {
}
