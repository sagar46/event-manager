package com.event_manager.EventManeger.form;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FormQuestionRepository extends JpaRepository<FormQuestion, Long> {
	List<FormQuestion> findByFormOrderBySortOrderAsc(Form form);
}
