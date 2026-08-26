package com.event_manager.EventManeger.form;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FormRepository extends JpaRepository<Form, Long> {
	List<Form> findByActiveTrueOrderByNameAsc();
}
