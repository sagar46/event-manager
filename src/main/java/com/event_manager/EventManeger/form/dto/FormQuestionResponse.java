package com.event_manager.EventManeger.form.dto;

import com.event_manager.EventManeger.form.QuestionType;

public record FormQuestionResponse(
		Long id,
		String prompt,
		QuestionType questionType,
		String optionsJson,
		boolean required,
		int sortOrder) {
}
