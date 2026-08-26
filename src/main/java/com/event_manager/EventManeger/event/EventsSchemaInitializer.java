package com.event_manager.EventManeger.event;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate {@code ddl-auto=update} does not always add new columns to an existing H2 file DB.
 * This runner adds attendance/catalog columns introduced after the original {@code events} table.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class EventsSchemaInitializer implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		addColumnIfMissing("ends_at", "TIMESTAMP WITH TIME ZONE");
		addColumnIfMissing("category_id", "BIGINT");
		addColumnIfMissing("event_type_id", "BIGINT");
		addColumnIfMissing("feedback_form_id", "BIGINT");
		addColumnIfMissing("check_in_grace_minutes", "INTEGER DEFAULT 15 NOT NULL");
		addColumnIfMissing("check_in_window_minutes_before", "INTEGER DEFAULT 60 NOT NULL");
		addColumnIfMissing("require_location_for_check_in", "BOOLEAN DEFAULT FALSE NOT NULL");
	}

	private void addColumnIfMissing(String columnName, String columnDefinition) {
		if (columnExists(columnName)) {
			return;
		}
		try {
			jdbcTemplate.execute("ALTER TABLE events ADD COLUMN " + columnName + " " + columnDefinition);
			log.info("Added missing events.{} column", columnName);
		} catch (Exception exception) {
			log.warn("Could not add events.{}: {}", columnName, exception.getMessage());
		}
	}

	private boolean columnExists(String columnName) {
		Integer count = jdbcTemplate.queryForObject(
				"""
						SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
						WHERE UPPER(TABLE_NAME) = 'EVENTS' AND UPPER(COLUMN_NAME) = ?
						""",
				Integer.class,
				columnName.toUpperCase());
		return count != null && count > 0;
	}
}
